package com.ethobservatory.controller;

import com.ethobservatory.dto.HealthResponse;
import com.ethobservatory.dto.StatusResponse;
import com.ethobservatory.dto.StatsResponse;
import com.ethobservatory.model.AddressActivity;
import com.ethobservatory.model.TransactionRecord;
import com.ethobservatory.repository.AddressActivityRepository;
import com.ethobservatory.repository.BlockRepository;
import com.ethobservatory.repository.TransactionRepository;
import com.ethobservatory.service.EthereumService;
import com.ethobservatory.service.SessionService;
import com.ethobservatory.config.ObservatoryProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final EthereumService ethereumService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;
    private final AddressActivityRepository addressActivityRepository;
    private final BlockRepository blockRepository;
    private final ObservatoryProperties observatoryProperties;

    public ApiController(EthereumService ethereumService,
                        SessionService sessionService,
                        TransactionRepository transactionRepository,
                        AddressActivityRepository addressActivityRepository,
                        BlockRepository blockRepository,
                        ObservatoryProperties observatoryProperties) {
        this.ethereumService = ethereumService;
        this.sessionService = sessionService;
        this.transactionRepository = transactionRepository;
        this.addressActivityRepository = addressActivityRepository;
        this.blockRepository = blockRepository;
        this.observatoryProperties = observatoryProperties;
    }

    @GetMapping("/status")
    public StatusResponse status() {
        Long latest = ethereumService.getLatestBlockNumber();
        Long chainId = ethereumService.getCurrentChainId();
        boolean configured = ethereumService.isConfigured();
        return new StatusResponse(
                configured ? (chainId == 1L ? "CONNECTED" : "WRONG_NETWORK") : "SIMULATED",
                configured ? (chainId == 1L ? "ETHEREUM MAINNET" : "UNKNOWN") : "LOCAL TEST MODE",
                configured ? "LIVE" : "SIMULATED",
                configured ? "LIVE" : "SIMULATED",
                latest,
                chainId,
                ethereumService.getProviderDisplayName(),
                ethereumService.getRpcLatencyMs()
        );
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        Long latest = ethereumService.getLatestBlockNumber();
        long observedTransactions = transactionRepository.countTransactions();
        Long uniqueWallets = transactionRepository.countDistinctWallets();
        BigDecimal ethVolume = transactionRepository.findAll().stream()
                .map(TransactionRecord::getEthValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long repeatedEntities = addressActivityRepository.findAll().stream()
                .filter(activity -> activity.getTransactionCount() >= observatoryProperties.getRepeatedThreshold())
                .count();
        Long blocksObserved = blockRepository.count();
        Long rpcLatencyMs = ethereumService.getRpcLatencyMs();
        BigInteger baseGas = blockRepository.findTopByOrderByBlockNumberDesc()
            .map(block -> block.getBaseFee() == null ? BigInteger.ZERO : block.getBaseFee())
            .orElse(BigInteger.ZERO);
        return new StatsResponse(
                latest,
                observedTransactions,
                ethVolume,
                uniqueWallets == null ? 0L : uniqueWallets,
                repeatedEntities,
                baseGas,
                rpcLatencyMs,
                ethereumService.isConfigured() ? "ETHEREUM MAINNET" : "LOCAL TEST MODE",
                ethereumService.isConfigured() ? "LIVE" : "SIMULATED",
                blocksObserved,
                sessionService.sessionUptimeSeconds()
        );
    }

    @GetMapping("/transactions")
    public Map<String, Object> transactions(@RequestParam(defaultValue = "8") int size) {
        int limit = Math.max(1, Math.min(size, observatoryProperties.getMaxVisibleTransactionRows()));
        List<TransactionRecord> records = transactionRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent();

        List<Map<String, Object>> content = new ArrayList<>();
        for (TransactionRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("timestamp", record.getTimestamp() == null ? Instant.EPOCH : record.getTimestamp());
            item.put("from", record.getFromAddress());
            item.put("to", record.getToAddress());
            item.put("value", record.getEthValue() == null ? BigDecimal.ZERO : record.getEthValue());
            item.put("gasPrice", record.getGasPriceWei() == null ? BigInteger.ZERO : record.getGasPriceWei());
            item.put("blockNumber", record.getBlockNumber());
            item.put("hash", record.getTransactionHash());
            content.add(item);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("content", content);
        payload.put("totalElements", transactionRepository.count());
        payload.put("pageSize", limit);
        return payload;
    }

    @GetMapping("/repeated")
    public List<Map<String, Object>> repeated() {
        return addressActivityRepository.findAll().stream()
                .filter(activity -> activity.getTransactionCount() >= observatoryProperties.getRepeatedThreshold())
                .sorted((left, right) -> Integer.compare(right.getTransactionCount(), left.getTransactionCount()))
                .limit(10)
                .map(activity -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("address", activity.getAddress());
                    item.put("transactionCount", activity.getTransactionCount());
                    item.put("ethSent", activity.getEthSent());
                    item.put("ethReceived", activity.getEthReceived());
                    return item;
                })
                .toList();
    }

    @GetMapping("/graph")
    public Map<String, Object> graph() {
        AtomicInteger nodeSequence = new AtomicInteger(1);
        List<Map<String, Object>> nodes = addressActivityRepository.findAll().stream()
                .sorted((left, right) -> Integer.compare(right.getTransactionCount(), left.getTransactionCount()))
                .limit(Math.max(5, Math.min(observatoryProperties.getMaxGraphNodes(), 500)))
                .map(activity -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("shortCode", "N" + String.format("%03d", nodeSequence.getAndIncrement()));
                    item.put("id", activity.getAddress());
                    item.put("label", activity.getAddress());
                    item.put("transactionCount", activity.getTransactionCount());
                    item.put("sentCount", activity.getSentTransactions());
                    item.put("receivedCount", activity.getReceivedTransactions());
                    item.put("isRepeated", activity.getTransactionCount() >= observatoryProperties.getRepeatedThreshold());
                    return item;
                })
                .toList();

        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Map<String, Object>> edgeMap = new LinkedHashMap<>();
        for (TransactionRecord transaction : transactionRepository.findAll()) {
            if (transaction.getFromAddress() == null || transaction.getToAddress() == null) {
                continue;
            }
            String key = transaction.getFromAddress() + "->" + transaction.getToAddress();
            Map<String, Object> edge = edgeMap.computeIfAbsent(key, ignored -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("source", transaction.getFromAddress());
                value.put("target", transaction.getToAddress());
                value.put("transactionCount", 0);
                value.put("totalEth", BigDecimal.ZERO);
                value.put("lastSeen", transaction.getTimestamp());
                return value;
            });
            int count = (Integer) edge.get("transactionCount") + 1;
            edge.put("transactionCount", count);
            BigDecimal total = (BigDecimal) edge.get("totalEth");
            BigDecimal value = transaction.getEthValue() == null ? BigDecimal.ZERO : transaction.getEthValue();
            edge.put("totalEth", total.add(value));
            edge.put("lastSeen", transaction.getTimestamp());
        }
        edgeMap.values().stream().limit(observatoryProperties.getMaxGraphEdges()).forEach(edges::add);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nodes", nodes);
        payload.put("edges", edges);
        payload.put("generatedAt", Instant.now());
        payload.put("blockNumber", ethereumService.getLatestBlockNumber());
        return payload;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q) {
        String query = q == null ? "" : q.trim();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", query);
        if (query.isBlank()) {
            payload.put("transactions", List.of());
            payload.put("addresses", List.of());
            payload.put("blocks", List.of());
            return payload;
        }

        List<TransactionRecord> matches = new ArrayList<>(
                transactionRepository.findByTransactionHashContainingIgnoreCaseOrFromAddressContainingIgnoreCaseOrToAddressContainingIgnoreCase(
                        query, query, query));
        try {
            matches.addAll(transactionRepository.findByBlockNumber(Long.parseLong(query)));
        } catch (NumberFormatException ignored) {
            // Non-numeric queries can only match transaction fields.
        }
        payload.put("transactions", matches.stream().distinct().limit(20).map(this::transactionPayload).toList());
        payload.put("addresses", addressActivityRepository.findAll().stream()
                .filter(activity -> activity.getAddress().toLowerCase().contains(query.toLowerCase()))
                .limit(20)
                .map(AddressActivity::getAddress)
                .toList());
        payload.put("blocks", blockRepository.findAll().stream()
                .filter(block -> String.valueOf(block.getBlockNumber()).equals(query)
                        || (block.getBlockHash() != null && block.getBlockHash().equalsIgnoreCase(query)))
                .limit(20)
                .map(block -> Map.of("number", block.getBlockNumber(), "hash", block.getBlockHash()))
                .toList());
        return payload;
    }

    private Map<String, Object> transactionPayload(TransactionRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("timestamp", record.getTimestamp() == null ? Instant.EPOCH : record.getTimestamp());
        item.put("from", record.getFromAddress());
        item.put("to", record.getToAddress());
        item.put("value", record.getEthValue() == null ? BigDecimal.ZERO : record.getEthValue());
        item.put("gasPrice", record.getGasPriceWei() == null ? BigInteger.ZERO : record.getGasPriceWei());
        item.put("blockNumber", record.getBlockNumber());
        item.put("hash", record.getTransactionHash());
        return item;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        try {
            Long chainId = ethereumService.getCurrentChainId();
            return new HealthResponse(
                    ethereumService.isConfigured() ? "UP" : "SIMULATED",
                    ethereumService.isConfigured() ? "UP" : "SIMULATED",
                    chainId == 1L ? "UP" : "DOWN",
                    ethereumService.isConfigured() ? "LIVE" : "SIMULATED"
            );
        } catch (Exception e) {
            return new HealthResponse("UP", "UP", "DOWN", "PAUSED");
        }
    }
}
