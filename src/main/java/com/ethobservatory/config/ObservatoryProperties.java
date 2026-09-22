package com.ethobservatory.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "observatory")
@Validated
public class ObservatoryProperties {

    @Min(1)
    private int historyBlockCount = 5;

    @Min(1)
    private int maxTransactions = 5000;

    @Min(1)
    private int maxBlocks = 100;

    @Min(1)
    private int maxGraphNodes = 500;

    @Min(1)
    private int maxGraphEdges = 800;

    @Min(1)
    private int repeatedThreshold = 2;

    @Min(1)
    private int maxVisibleTransactionRows = 100;

    public int getHistoryBlockCount() {
        return historyBlockCount;
    }

    public void setHistoryBlockCount(int historyBlockCount) {
        this.historyBlockCount = historyBlockCount;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public void setMaxTransactions(int maxTransactions) {
        this.maxTransactions = maxTransactions;
    }

    public int getMaxBlocks() {
        return maxBlocks;
    }

    public void setMaxBlocks(int maxBlocks) {
        this.maxBlocks = maxBlocks;
    }

    public int getMaxGraphNodes() {
        return maxGraphNodes;
    }

    public void setMaxGraphNodes(int maxGraphNodes) {
        this.maxGraphNodes = maxGraphNodes;
    }

    public int getMaxGraphEdges() {
        return maxGraphEdges;
    }

    public void setMaxGraphEdges(int maxGraphEdges) {
        this.maxGraphEdges = maxGraphEdges;
    }

    public int getRepeatedThreshold() {
        return repeatedThreshold;
    }

    public void setRepeatedThreshold(int repeatedThreshold) {
        this.repeatedThreshold = repeatedThreshold;
    }

    public int getMaxVisibleTransactionRows() {
        return maxVisibleTransactionRows;
    }

    public void setMaxVisibleTransactionRows(int maxVisibleTransactionRows) {
        this.maxVisibleTransactionRows = maxVisibleTransactionRows;
    }
}
