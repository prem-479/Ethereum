package com.ethobservatory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GraphResponse(
        List<NodeDto> nodes,
        List<EdgeDto> edges,
        Instant generatedAt,
        Long blockNumber
) {
    public record NodeDto(
            String id,
            String label,
            int transactionCount,
            int sentCount,
            int receivedCount,
            boolean isRepeated
    ) {}

    public record EdgeDto(
            String source,
            String target,
            int transactionCount,
            BigDecimal totalEth,
            Instant lastSeen
    ) {}
}
