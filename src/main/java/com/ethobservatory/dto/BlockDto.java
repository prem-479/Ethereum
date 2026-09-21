package com.ethobservatory.dto;

import java.math.BigInteger;
import java.time.Instant;

public record BlockDto(
        Long blockNumber,
        String blockHash,
        String parentHash,
        Instant timestamp,
        BigInteger gasLimit,
        BigInteger gasUsed,
        BigInteger baseFee,
        Integer transactionCount
) {
}
