package me.kripanshu.backend.dto;

public record UpdateEndPointRequest(
        String path,
        String method,
        String headers,
        String queryParams,
        String bodySchema,
        String samplePayload
) {}