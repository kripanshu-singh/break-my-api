package me.kripanshu.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBaselineRequest(
        @NotNull
        @Min(100)
        @Max(599)
        Integer expectedStatus,

        String expectedSchema,
        String sampleResponse,
        Long maxLatencyMs
) { }
