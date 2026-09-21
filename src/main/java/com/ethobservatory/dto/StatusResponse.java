package com.ethobservatory.dto;

public record StatusResponse(
        String status,
        String network,
        String dataSource,
        String blockStatus,
        Long latestBlock,
        Long chainId,
        String provider,
        Long rpcLatencyMs
) {
}
