package me.kripanshu.backend.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String resourceId;

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " not found: " + resourceId);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
}
