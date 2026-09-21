package com.ethobservatory.model;

import jakarta.persistence.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blocks", indexes = {
        @Index(name = "idx_blocks_number", columnList = "blockNumber"),
        @Index(name = "idx_blocks_timestamp", columnList = "timestamp")
})
public class BlockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_number", nullable = false, unique = true)
    private Long blockNumber;

    @Column(name = "block_hash", nullable = false, unique = true)
    private String blockHash;

    @Column(name = "parent_hash", nullable = false)
    private String parentHash;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "gas_limit", precision = 50)
    private BigInteger gasLimit;

    @Column(name = "gas_used", precision = 50)
    private BigInteger gasUsed;

    @Column(name = "base_fee", precision = 50)
    private BigInteger baseFee;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionRecord> transactions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigInteger getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigInteger baseFee) {
        this.baseFee = baseFee;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<TransactionRecord> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionRecord> transactions) {
        this.transactions = transactions;
    }
}
