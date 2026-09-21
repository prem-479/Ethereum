package com.ethobservatory.service;

import com.ethobservatory.config.EthereumProperties;
import com.ethobservatory.config.ObservatoryProperties;
import com.ethobservatory.model.BlockRecord;
import com.ethobservatory.repository.BlockRepository;
import com.ethobservatory.websocket.LiveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BlockchainMonitorService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainMonitorService.class);

    private final EthereumService ethereumService;
    private final TransactionProcessingService transactionProcessingService;
    private final BlockRepository blockRepository;
    private final EthereumProperties ethereumProperties;
    private final ObservatoryProperties observatoryProperties;
    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicLong lastProcessedBlock = new AtomicLong(0L);
    private volatile String status = "INITIALIZING";

    public BlockchainMonitorService(EthereumService ethereumService,
                                   TransactionProcessingService transactionProcessingService,
                                   BlockRepository blockRepository,
                                   EthereumProperties ethereumProperties,
                                   ObservatoryProperties observatoryProperties,
                                   SimpMessagingTemplate messagingTemplate) {
        this.ethereumService = ethereumService;
        this.transactionProcessingService = transactionProcessingService;
        this.blockRepository = blockRepository;
        this.ethereumProperties = ethereumProperties;
        this.observatoryProperties = observatoryProperties;
        this.messagingTemplate = messagingTemplate;
    }

    public void initialize() {
        if (!ethereumService.isConfigured()) {
            status = "DISCONNECTED";
            return;
        }

        try {
            long chainId = ethereumService.getCurrentChainId();
            if (chainId != 1L) {
                status = "WRONG_NETWORK";
                return;
            }

            long latestBlock = ethereumService.getLatestBlockNumber();
            status = "LOADING HISTORY";
            Long maxRecorded = blockRepository.findMaxBlockNumber();
            if (maxRecorded == null) {
                maxRecorded = 0L;
            }

            long start = Math.max(0, latestBlock - observatoryProperties.getHistoryBlockCount() + 1);
            List<Long> gaps = new ArrayList<>();
            for (long i = start; i <= latestBlock; i++) {
                gaps.add(i);
            }

            for (Long blockNumber : gaps) {
                if (blockRepository.findByBlockNumber(blockNumber).isEmpty()) {
                    EthBlock.Block block = ethereumService.getBlockByNumber(blockNumber);
                    transactionProcessingService.processBlock(block);
                }
            }

            lastProcessedBlock.set(latestBlock);
            status = "LIVE";
            publishBlockEvent(latestBlock);
            log.info("Blockchain monitor is now LIVE at block {}", latestBlock);
        } catch (Exception e) {
            status = "DISCONNECTED";
            log.error("Unable to initialize blockchain monitor", e);
        }
    }

    @Scheduled(fixedDelayString = "${ethereum.polling-interval-ms:3000}")
    public void pollForNewBlocks() {
        if ("INITIALIZING".equals(status) || "LOADING HISTORY".equals(status)) {
            return;
        }
        if (!ethereumService.isConfigured()) {
            status = "DISCONNECTED";
            return;
        }

        try {
            long latest = ethereumService.getLatestBlockNumber();
            long current = lastProcessedBlock.get();
            if (latest <= current) {
                return;
            }

            status = "CATCHING UP";
            for (long blockNumber = current + 1; blockNumber <= latest; blockNumber++) {
                EthBlock.Block block = ethereumService.getBlockByNumber(blockNumber);
                transactionProcessingService.processBlock(block);
                lastProcessedBlock.set(blockNumber);
                publishBlockEvent(blockNumber);
            }
            status = "LIVE";
        } catch (Exception e) {
            status = "DISCONNECTED";
            log.error("Blockchain monitor encountered an RPC error", e);
        }
    }

    public String getStatus() {
        return status;
    }

    public long getLastProcessedBlock() {
        return lastProcessedBlock.get();
    }

    private void publishBlockEvent(long blockNumber) {
        messagingTemplate.convertAndSend("/topic/status", new LiveEvent(
                "BLOCK_PROCESSED",
                java.util.Map.of("blockNumber", blockNumber, "status", status)));
    }
}
