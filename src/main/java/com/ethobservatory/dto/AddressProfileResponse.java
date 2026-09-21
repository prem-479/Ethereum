package com.ethobservatory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AddressProfileResponse(
        String address,
        int transactionCount,
        int sentTransactions,
        int receivedTransactions,
        int uniqueCounterparties,
        BigDecimal ethSent,
        BigDecimal ethReceived,
        Instant firstSeen,
        Instant lastSeen
) {
}
