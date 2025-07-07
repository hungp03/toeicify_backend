package com.toeicify.toeic.exception;

/**
 * Created by hungpham on 7/7/2025
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
