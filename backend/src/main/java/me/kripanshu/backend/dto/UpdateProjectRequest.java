package me.kripanshu.backend.dto;

public record UpdateProjectRequest(
        String name,
        String description,
        String baseUrl,
        String version
) {}
