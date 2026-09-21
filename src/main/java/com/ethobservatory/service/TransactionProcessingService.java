package com.ethobservatory.service;

import com.ethobservatory.model.BlockRecord;
import com.ethobservatory.model.TransactionRecord;
import com.ethobservatory.repository.BlockRepository;
import com.ethobservatory.repository.TransactionRepository;
import com.ethobservatory.util.WeiConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);
    private static final long MAINNET_LONDON_BLOCK = 12_965_000L;

    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final AddressActivityService addressActivityService;

    public TransactionProcessingService(BlockRepository blockRepository,
                                       TransactionRepository transactionRepository,
                                       AddressActivityService addressActivityService) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.addressActivityService = addressActivityService;
    }

    @Transactional
    public void processBlock(EthBlock.Block block) {
        if (block == null || block.getNumber() == null) {
            throw new IllegalArgumentException("Block data is missing required values.");
        }

        Long blockNumber = block.getNumber().longValue();
        if (blockRepository.findByBlockNumber(blockNumber).isPresent()) {
            log.debug("Skipping duplicate block {}", blockNumber);
            return;
        }

        BlockRecord blockRecord = new BlockRecord();
        blockRecord.setBlockNumber(blockNumber);
        blockRecord.setBlockHash(block.getHash());
        blockRecord.setParentHash(block.getParentHash());
        blockRecord.setTimestamp(Instant.ofEpochSecond(block.getTimestamp().longValue()));
        blockRecord.setGasLimit(block.getGasLimit());
        blockRecord.setGasUsed(block.getGasUsed());
        blockRecord.setBaseFee(readBaseFee(block));
        blockRecord.setTransactionCount(block.getTransactions() == null ? 0 : block.getTransactions().size());
        blockRecord = blockRepository.save(blockRecord);

        if (block.getTransactions() == null || block.getTransactions().isEmpty()) {
            return;
        }

        for (Object result : block.getTransactions()) {
            if (!(result instanceof EthBlock.TransactionObject tx)) {
                continue;
            }

            String hash = tx.getHash();
            if (transactionRepository.findByTransactionHash(hash).isPresent()) {
                log.debug("Skipping duplicate transaction {}", hash);
                continue;
            }

            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setTransactionHash(hash);
            transactionRecord.setFromAddress(tx.getFrom());
            transactionRecord.setToAddress(tx.getTo());
            transactionRecord.setValueWei(tx.getValue());
            transactionRecord.setGasPriceWei(tx.getGasPrice());
            transactionRecord.setGas(tx.getGas());
            transactionRecord.setNonce(tx.getNonce());
            transactionRecord.setBlockNumber(blockNumber);
            transactionRecord.setBlockHash(block.getHash());
            transactionRecord.setTransactionIndex(tx.getTransactionIndex() == null ? 0 : tx.getTransactionIndex().intValue());
            transactionRecord.setInputData(tx.getInput());
            transactionRecord.setTransactionType(tx.getType() == null ? "0x0" : tx.getType());
            transactionRecord.setTimestamp(Instant.ofEpochSecond(block.getTimestamp().longValue()));
            transactionRecord.setStatus("PENDING");
            transactionRecord.setEthValue(WeiConverter.weiToEth(tx.getValue()));
            transactionRecord.setBlock(blockRecord);

            transactionRepository.save(transactionRecord);

            if (tx.getFrom() != null) {
                addressActivityService.recordActivity(tx.getFrom(), true, WeiConverter.weiToEth(tx.getValue()), transactionRecord.getTimestamp(), tx.getTo());
            }
            if (tx.getTo() != null) {
                addressActivityService.recordActivity(tx.getTo(), false, WeiConverter.weiToEth(tx.getValue()), transactionRecord.getTimestamp(), tx.getFrom());
            }
        }
    }

    public List<TransactionRecord> findRecentTransactions(int limit) {
        return transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    private BigInteger readBaseFee(EthBlock.Block block) {
        if (block.getNumber() != null && block.getNumber().longValue() < MAINNET_LONDON_BLOCK) {
            return BigInteger.ZERO;
        }
        try {
            BigInteger baseFee = block.getBaseFeePerGas();
            return baseFee == null ? BigInteger.ZERO : baseFee;
        } catch (MessageDecodingException e) {
            log.debug("Ignoring malformed baseFeePerGas for block {}", block.getNumber());
            return BigInteger.ZERO;
        }
    }
}
