package com.toeicify.toeic.exception;

/**
 * Created by hungpham on 8/12/2025
 */
public class AccessDeniedException extends RuntimeException {
  public AccessDeniedException(String message) {
    super(message);
  }
}
