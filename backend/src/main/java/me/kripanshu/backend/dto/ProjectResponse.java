package me.kripanshu.backend.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
        String id,
        String name,
        String description,
        String baseUrl,
        String version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
