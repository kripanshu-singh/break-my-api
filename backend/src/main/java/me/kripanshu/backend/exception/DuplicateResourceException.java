package me.kripanshu.backend.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String resourceName;
    private final String field;
    private final String value;

    public DuplicateResourceException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' already exists");
        this.resourceName = resourceName;
        this.field = field;
        this.value = value;
    }
}
