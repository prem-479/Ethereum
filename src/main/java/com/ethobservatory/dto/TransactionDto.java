package com.ethobservatory.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record TransactionDto(
        String hash,
        String from,
        String to,
        BigDecimal value,
        BigInteger gasPrice,
        BigInteger gas,
        BigInteger nonce,
        Long blockNumber,
        String blockHash,
        Integer transactionIndex,
        String input,
        String type,
        Instant timestamp,
        String status,
        BigInteger gasUsed
) {
}
