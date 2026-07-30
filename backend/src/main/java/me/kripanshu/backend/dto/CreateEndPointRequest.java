package me.kripanshu.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateEndPointRequest(
        @NotBlank
        @Pattern(regexp = "^/.*", message = "Path must start with /")
        String path,

        @NotBlank
        @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|QUERY)$",
                message = "Method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, QUERY")
        String method,

        String headers,
        String queryParams,
        String bodySchema
) { }
