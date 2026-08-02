package me.kripanshu.backend.dto;

public record UpdateTestSuiteRequest(
        String name,
        TestSuiteConfig configuration
) {}
