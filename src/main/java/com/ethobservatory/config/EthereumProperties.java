package com.ethobservatory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "ethereum")
@Validated
public class EthereumProperties {

    private String rpcUrl = "";

    private String wsUrl = "";

    private Long chainId = 1L;

    private Long pollingIntervalMs = 3000L;

    private Long requestTimeoutMs = 10000L;

    private Long confirmations = 0L;

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public Long getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    public void setPollingIntervalMs(Long pollingIntervalMs) {
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public Long getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(Long requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public Long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Long confirmations) {
        this.confirmations = confirmations;
    }
}
