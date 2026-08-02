package me.kripanshu.backend.dto;

public record ProbeResult(
        String id,
        String endpointId,
        Integer actualStatus,
        Long actualLatencyMs,
        String actualResponseBody,
        String derivedSchema
) { }
