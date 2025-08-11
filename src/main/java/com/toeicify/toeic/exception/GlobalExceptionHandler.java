package com.toeicify.toeic.exception;

import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.util.constant.ErrorCode;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 7/7/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleCredentialException(RuntimeException ex) {
        return buildResponse(ErrorCode.BAD_CREDENTIALS, "Authentication exception", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(RuntimeException ex) {
        return buildResponse(ErrorCode.UNAUTHORIZED, "Unauthorized exception", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(RuntimeException ex) {
            return buildResponse(ErrorCode.FORBIDDEN, "Access denied exception", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceInvalidException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceInvalidException(RuntimeException ex) {
        return buildResponse(ErrorCode.RESOURCE_INVALID, "Resource Invalid Exception", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(RuntimeException ex) {
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, "Resource Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceAlreadyExistsException(RuntimeException ex) {
        return buildResponse(ErrorCode.RESOURCE_ALREADY_EXISTS, "Resource already exists", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        return buildResponse(ErrorCode.METHOD_NOT_VALID, "Validation Error",
                errors.size() > 1 ? errors : errors.getFirst(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND.value(), "404 Not Found. URL may not exist...",
                ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannotDeleteException.class)
    public ResponseEntity<ApiResponse<Object>> handleCannotDeleteException(CannotDeleteException ex) {
        return buildResponse(ErrorCode.CANNOT_DELETE, "Cannot delete resource", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(RuntimeException ex) {
        return buildResponse(ErrorCode.EXCEPTION, "Server Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(int statusCode, String error, Object message, HttpStatus status) {
        ApiResponse<Object> response = ApiResponse.error(statusCode, message, error);
        return ResponseEntity.status(status).body(response);
    }
}
