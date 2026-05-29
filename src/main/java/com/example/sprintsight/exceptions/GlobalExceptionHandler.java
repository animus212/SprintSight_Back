package com.example.sprintsight.exceptions;

import com.example.sprintsight.dtos.responses.ApiError;
import com.example.sprintsight.dtos.responses.FieldValidationError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex,HttpServletRequest request) {
        log.warn("Failed login attempt for {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledAccount(DisabledException ex, HttpServletRequest request) {
        log.warn("Login attempt on disabled account at {}", request.getRequestURI());

        return build(HttpStatus.FORBIDDEN, "Account is disabled", request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLockedAccount(LockedException ex, HttpServletRequest request) {
        log.warn("Login attempt on locked account at {}", request.getRequestURI());

        return build(HttpStatus.LOCKED, "Account is locked", request);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiError> handleTokenRefresh(TokenRefreshException ex, HttpServletRequest request) {
        log.warn("Token refresh failure at {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
       return build(HttpStatus.FORBIDDEN, ex.getMessage() != null ? ex.getMessage() : "Access denied", request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiError> handleResourceConflict(ResourceConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("IllegalStateException at {} — consider migrating to a domain exception: {}",
                request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String friendlyMessage = ConstraintViolationTranslator.translate(ex);

        if (friendlyMessage != null) {
            return build(HttpStatus.CONFLICT, friendlyMessage, request);
        }

        String constraintName = ConstraintViolationTranslator.extractConstraintName(ex);

        log.error("Unhandled DB constraint violation at {} (constraint={}): {}",
                request.getRequestURI(), constraintName, rootMessage(ex));

        return build(HttpStatus.CONFLICT, "This operation conflicts with existing data", request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request
    ) {
        log.warn("Optimistic lock failure at {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.CONFLICT,
                "This resource was modified by another request. Please refresh and try again.", request);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> handleDbUnreachable(
            DataAccessResourceFailureException ex,
            HttpServletRequest request
    ) {
        log.error("Database unreachable at {}", request.getRequestURI(), ex);

        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable. Please try again.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        List<FieldValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldValidationError(
                        err.getField().isEmpty() ? "_global" : err.getField(),
                        err.getDefaultMessage()))
                .toList();

        ApiError body = buildPayload(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                requestPath(request),
                requestMethod(request),
                errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleParamValidation(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new FieldValidationError(
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : "_global",
                        v.getMessage()))
                .toList();

        ApiError body = buildPayload(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request.getRequestURI(),
                request.getMethod(),
                errors);

        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ApiError body = buildPayload(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON in request body",
                requestPath(request),
                requestMethod(request),
                null);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ApiError body = buildPayload(
                HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing",
                requestPath(request),
                requestMethod(request),
                null);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ApiError body = buildPayload(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method " + ex.getMethod() + " not supported for this endpoint",
                requestPath(request),
                requestMethod(request),
                null);

        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ApiError body = buildPayload(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type " + ex.getContentType() + " is not supported",
                requestPath(request),
                requestMethod(request),
                null);

        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ApiError body = buildPayload(
                HttpStatus.NOT_FOUND,
                "No endpoint at " + ex.getRequestURL(),
                requestPath(request),
                requestMethod(request),
                null);

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";

        return build(HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has invalid value — expected " + required, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiError> handleInvalidImage(InvalidImageException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE,
                "Uploaded file is too large. Maximum size is 5MB.",
                request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(buildPayload(
                status, message, request.getRequestURI(), request.getMethod(), null));
    }

    private ApiError buildPayload(
            HttpStatus status,
            String message,
            String path,
            String method,
            List<FieldValidationError> fieldErrors
    ) {
        return new ApiError(
                message,
                status.value(),
                path,
                method,
                Instant.now(),
                fieldErrors,
                MDC.get("requestId")
        );
    }

    private static String rootMessage(Throwable t) {
        Throwable root = t;

        while (root.getCause() != null && root.getCause() != root) root = root.getCause();

        return root.getMessage();
    }

    private static String requestPath(WebRequest request) {
        String desc = request.getDescription(false);

        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

    private static String requestMethod(WebRequest request) {
        return request.getHeader("X-Forwarded-Method") != null
                ? request.getHeader("X-Forwarded-Method")
                : "?";
    }
}
