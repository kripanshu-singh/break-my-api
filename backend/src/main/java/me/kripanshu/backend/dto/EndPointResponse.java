package me.kripanshu.backend.dto;

import java.time.LocalDateTime;

public record EndPointResponse(
        String id,
        String projectId,
        String path,
        String method,
        String headers,
        String queryParams,
        String bodySchema,
        String samplePayload,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
