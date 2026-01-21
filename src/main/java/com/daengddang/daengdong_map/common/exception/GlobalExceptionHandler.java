package com.daengddang.daengdong_map.common.exception;

import com.daengddang.daengdong_map.common.ApiResponse;
import com.daengddang.daengdong_map.common.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        ErrorCode errorCode = resolveValidationError(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode errorCode = resolveConstraintViolation(e);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private ErrorCode resolveValidationError(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return ErrorCode.INVALID_FORMAT;
        }

        String field = fieldError.getField();
        String code = fieldError.getCode();

        if ("name".equals(field)) {
            if ("NotBlank".equals(code)) {
                return ErrorCode.NAME_REQUIRED;
            }
            if ("Pattern".equals(code) || "Size".equals(code)) {
                return ErrorCode.NAME_RULE_VIOLATION;
            }
        }

        if ("breedId".equals(field) && "NotNull".equals(code)) {
            return ErrorCode.DOG_BREED_REQUIRED;
        }

        if ("birthDate".equals(field) && "NotNull".equals(code)) {
            return ErrorCode.DOG_BIRTHDATE_REQUIRED;
        }

        if ("weight".equals(field)) {
            if ("NotNull".equals(code)) {
                return ErrorCode.DOG_WEIGHT_REQUIRED;
            }
            if ("Digits".equals(code)) {
                return ErrorCode.DOG_WEIGHT_DECIMAL_LIMIT;
            }
        }

        return ErrorCode.INVALID_FORMAT;
    }

    private ErrorCode resolveConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConstraintViolationException constraintViolation) {
            String constraintName = constraintViolation.getConstraintName();
            if ("uk_walks_in_progress_dog".equalsIgnoreCase(constraintName)) {
                return ErrorCode.WALK_ALREADY_IN_PROGRESS;
            }
            if ("uk_breed_name".equalsIgnoreCase(constraintName)) {
                return ErrorCode.DOG_BREED_NAME_DUPLICATED;
            }
        }
        return ErrorCode.INVALID_FORMAT;
    }
}
