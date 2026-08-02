package me.kripanshu.backend.dto;

import java.time.LocalDateTime;

public record TestSuiteResponse(
        String id,
        String projectId,
        String name,
        TestSuiteConfig configuration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
