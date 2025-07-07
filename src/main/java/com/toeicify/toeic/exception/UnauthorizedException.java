package com.toeicify.toeic.exception;

/**
 * Created by hungpham on 7/7/2025
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
