package com.ethobservatory.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public record StatsResponse(
        Long latestBlock,
        Long transactionsObserved,
        BigDecimal ethVolume,
        Long uniqueWallets,
        Long repeatedEntities,
        BigInteger baseGas,
        Long rpcLatencyMs,
        String networkStatus,
        String statusMessage,
        Long blocksObserved,
        Long sessionUptimeSeconds
) {
}
