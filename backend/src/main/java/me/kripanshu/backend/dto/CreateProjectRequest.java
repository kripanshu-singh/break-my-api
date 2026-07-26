package me.kripanshu.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String baseUrl,
        String version
) {}
