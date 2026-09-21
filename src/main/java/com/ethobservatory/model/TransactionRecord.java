package com.ethobservatory.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_hash", columnList = "transactionHash", unique = true),
        @Index(name = "idx_tx_block_number", columnList = "blockNumber"),
        @Index(name = "idx_tx_from", columnList = "fromAddress"),
        @Index(name = "idx_tx_to", columnList = "toAddress"),
        @Index(name = "idx_tx_timestamp", columnList = "timestamp")
})
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "from_address", nullable = false, length = 42)
    private String fromAddress;

    @Column(name = "to_address", length = 42)
    private String toAddress;

    @Column(name = "value_wei", precision = 80)
    private BigInteger valueWei;

    @Column(name = "gas_price_wei", precision = 80)
    private BigInteger gasPriceWei;

    @Column(name = "gas", precision = 50)
    private BigInteger gas;

    @Column(name = "nonce", precision = 50)
    private BigInteger nonce;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "block_hash", nullable = false)
    private String blockHash;

    @Column(name = "transaction_index")
    private Integer transactionIndex;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "status")
    private String status;

    @Column(name = "gas_used", precision = 50)
    private BigInteger gasUsed;

    @Column(name = "eth_value")
    private BigDecimal ethValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private BlockRecord block;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getValueWei() {
        return valueWei;
    }

    public void setValueWei(BigInteger valueWei) {
        this.valueWei = valueWei;
    }

    public BigInteger getGasPriceWei() {
        return gasPriceWei;
    }

    public void setGasPriceWei(BigInteger gasPriceWei) {
        this.gasPriceWei = gasPriceWei;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
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

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigDecimal getEthValue() {
        return ethValue;
    }

    public void setEthValue(BigDecimal ethValue) {
        this.ethValue = ethValue;
    }

    public BlockRecord getBlock() {
        return block;
    }

    public void setBlock(BlockRecord block) {
        this.block = block;
    }
}
