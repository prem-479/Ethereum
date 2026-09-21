package com.ethobservatory.dto;

public record HealthResponse(
        String application,
        String database,
        String ethereum,
        String blockMonitor
) {
}
