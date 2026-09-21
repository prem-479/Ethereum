package com.ethobservatory.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "address_activity", indexes = {
        @Index(name = "idx_address_activity_address", columnList = "address"),
        @Index(name = "idx_address_activity_last_seen", columnList = "lastSeen")
})
public class AddressActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false, length = 42)
    private String address;

    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    @Column(name = "sent_transactions")
    private Integer sentTransactions = 0;

    @Column(name = "received_transactions")
    private Integer receivedTransactions = 0;

    @Column(name = "unique_counterparties")
    private Integer uniqueCounterparties = 0;

    @Column(name = "eth_sent", precision = 38, scale = 18)
    private BigDecimal ethSent = BigDecimal.ZERO;

    @Column(name = "eth_received", precision = 38, scale = 18)
    private BigDecimal ethReceived = BigDecimal.ZERO;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Integer getSentTransactions() {
        return sentTransactions;
    }

    public void setSentTransactions(Integer sentTransactions) {
        this.sentTransactions = sentTransactions;
    }

    public Integer getReceivedTransactions() {
        return receivedTransactions;
    }

    public void setReceivedTransactions(Integer receivedTransactions) {
        this.receivedTransactions = receivedTransactions;
    }

    public Integer getUniqueCounterparties() {
        return uniqueCounterparties;
    }

    public void setUniqueCounterparties(Integer uniqueCounterparties) {
        this.uniqueCounterparties = uniqueCounterparties;
    }

    public BigDecimal getEthSent() {
        return ethSent;
    }

    public void setEthSent(BigDecimal ethSent) {
        this.ethSent = ethSent;
    }

    public BigDecimal getEthReceived() {
        return ethReceived;
    }

    public void setEthReceived(BigDecimal ethReceived) {
        this.ethReceived = ethReceived;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Instant firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
