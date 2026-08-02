package me.kripanshu.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTestSuiteRequest(
        @NotBlank String name,
        TestSuiteConfig configuration
) {}
