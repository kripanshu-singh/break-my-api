package me.kripanshu.backend.dto;

import java.time.LocalDateTime;

public record ExpectedResponseResponse(
        String id,
        String endpointId,
        Integer expectedStatus,
        String expectedSchema,
        String sampleResponse,
        Long maxLatencyMs,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
