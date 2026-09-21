package com.ethobservatory.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "observation_sessions")
public class ObservationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_start", nullable = false)
    private Instant sessionStart;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "latest_block")
    private Long latestBlock;

    @Column(name = "blocks_observed")
    private Long blocksObserved = 0L;

    @Column(name = "transactions_observed")
    private Long transactionsObserved = 0L;

    @Column(name = "unique_wallets")
    private Long uniqueWallets = 0L;

    @Column(name = "eth_volume_wei", precision = 80)
    private java.math.BigInteger ethVolumeWei = java.math.BigInteger.ZERO;

    @Column(name = "repeated_addresses")
    private Long repeatedAddresses = 0L;

    @Column(name = "average_rpc_latency_ms")
    private Double averageRpcLatencyMs = 0.0;

    @Column(name = "average_block_interval_ms")
    private Double averageBlockIntervalMs = 0.0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Instant sessionStart) {
        this.sessionStart = sessionStart;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getLatestBlock() {
        return latestBlock;
    }

    public void setLatestBlock(Long latestBlock) {
        this.latestBlock = latestBlock;
    }

    public Long getBlocksObserved() {
        return blocksObserved;
    }

    public void setBlocksObserved(Long blocksObserved) {
        this.blocksObserved = blocksObserved;
    }

    public Long getTransactionsObserved() {
        return transactionsObserved;
    }

    public void setTransactionsObserved(Long transactionsObserved) {
        this.transactionsObserved = transactionsObserved;
    }

    public Long getUniqueWallets() {
        return uniqueWallets;
    }

    public void setUniqueWallets(Long uniqueWallets) {
        this.uniqueWallets = uniqueWallets;
    }

    public java.math.BigInteger getEthVolumeWei() {
        return ethVolumeWei;
    }

    public void setEthVolumeWei(java.math.BigInteger ethVolumeWei) {
        this.ethVolumeWei = ethVolumeWei;
    }

    public Long getRepeatedAddresses() {
        return repeatedAddresses;
    }

    public void setRepeatedAddresses(Long repeatedAddresses) {
        this.repeatedAddresses = repeatedAddresses;
    }

    public Double getAverageRpcLatencyMs() {
        return averageRpcLatencyMs;
    }

    public void setAverageRpcLatencyMs(Double averageRpcLatencyMs) {
        this.averageRpcLatencyMs = averageRpcLatencyMs;
    }

    public Double getAverageBlockIntervalMs() {
        return averageBlockIntervalMs;
    }

    public void setAverageBlockIntervalMs(Double averageBlockIntervalMs) {
        this.averageBlockIntervalMs = averageBlockIntervalMs;
    }
}
