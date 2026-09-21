package com.ethobservatory.service;

import com.ethobservatory.config.EthereumProperties;
import com.ethobservatory.util.WeiConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EthereumService {

    private static final Logger log = LoggerFactory.getLogger(EthereumService.class);

    private final EthereumProperties properties;
    private final AtomicReference<Web3j> web3j = new AtomicReference<>();
    private volatile Long lastKnownBlockNumber = 0L;
    private volatile Long rpcLatencyMs = 0L;

    public EthereumService(EthereumProperties properties) {
        this.properties = properties;
        if (properties.getRpcUrl() != null && !properties.getRpcUrl().isBlank()) {
            web3j.set(Web3j.build(new HttpService(properties.getRpcUrl())));
            log.info("Configured Ethereum RPC client for {}", redactEndpoint(properties.getRpcUrl()));
        }
    }

    public Web3j getWeb3j() {
        return web3j.get();
    }

    public boolean isConfigured() {
        return web3j.get() != null;
    }

    public Long getCurrentChainId() {
        if (web3j.get() == null) {
            return 1L;
        }
        long start = System.currentTimeMillis();
        try {
            EthChainId chainId = web3j.get().ethChainId().send();
            rpcLatencyMs = System.currentTimeMillis() - start;
            if (chainId == null || chainId.getChainId() == null) {
                throw new IllegalStateException("eth_chainId returned null.");
            }
            return chainId.getChainId().longValue();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call eth_chainId", e);
        }
    }

    public Long getLatestBlockNumber() {
        if (web3j.get() == null) {
            return lastKnownBlockNumber;
        }
        try {
            long start = System.currentTimeMillis();
            EthBlockNumber response = web3j.get().ethBlockNumber().send();
            rpcLatencyMs = System.currentTimeMillis() - start;
            if (response == null || response.getBlockNumber() == null) {
                throw new IllegalStateException("eth_blockNumber returned null.");
            }
            lastKnownBlockNumber = response.getBlockNumber().longValue();
            return lastKnownBlockNumber;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to query eth_blockNumber", e);
        }
    }

    public EthBlock.Block getBlockByNumber(Long blockNumber) {
        if (web3j.get() == null) {
            throw new IllegalStateException("Ethereum RPC client is not configured.");
        }
        try {
            long start = System.currentTimeMillis();
            EthBlock block = web3j.get().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), true).send();
            rpcLatencyMs = System.currentTimeMillis() - start;
            if (block == null || block.getResult() == null) {
                throw new IllegalStateException("eth_getBlockByNumber returned null for block " + blockNumber);
            }
            return block.getResult();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to fetch block " + blockNumber, e);
        }
    }

    public Long getRpcLatencyMs() {
        return rpcLatencyMs;
    }

    public String getProviderDisplayName() {
        if (web3j.get() == null) {
            return "LOCAL TEST MODE";
        }
        return properties.getRpcUrl().contains("publicnode") ? "PublicNode Ethereum RPC" : "Ethereum Mainnet RPC";
    }

    public BigInteger parseWei(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? BigInteger.ZERO : new BigInteger(rawValue);
    }

    public BigDecimal toEth(BigInteger wei) {
        return WeiConverter.weiToEth(wei);
    }

    public boolean isMainnet() {
        return getCurrentChainId() == 1L;
    }

    private String redactEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                    + (uri.getPath() == null ? "" : uri.getPath().replaceAll("/v2/[^/]+", "/v2/[REDACTED]"));
        } catch (IllegalArgumentException e) {
            return "configured RPC endpoint";
        }
    }
}
