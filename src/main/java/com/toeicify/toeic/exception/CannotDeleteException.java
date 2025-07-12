package com.toeicify.toeic.exception;

/**
 * Created by hungpham on 7/12/2025
 */
public class CannotDeleteException extends RuntimeException {
    public CannotDeleteException(String message) {
        super(message);
    }
}
