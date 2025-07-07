package com.toeicify.toeic.exception;

/**
 * Created by hungpham on 7/7/2025
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
