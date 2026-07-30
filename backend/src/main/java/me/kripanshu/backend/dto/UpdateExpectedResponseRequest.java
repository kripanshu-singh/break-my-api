package me.kripanshu.backend.dto;

public record UpdateExpectedResponseRequest(
        Integer expectedStatus,
        String expectedSchema,
        String sampleResponse,
        Long maxLatencyMs
) { }
