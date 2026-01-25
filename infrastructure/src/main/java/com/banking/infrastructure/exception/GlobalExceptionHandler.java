package com.banking.infrastructure.exception;

import com.banking.domain.exception.DomainException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler implementing RFC 7807 Problem Details.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception) {
        log.debug("Resource not found: {}", exception.getMessage());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problem.setProperty("resourceType", exception.getResourceType());
        problem.setProperty("resourceId", exception.getResourceId());

        return problem;
    }

    @ExceptionHandler(DuplicateImportException.class)
    public ProblemDetail handleDuplicateImport(DuplicateImportException exception) {
        log.debug("Duplicate import attempt: {}", exception.getMessage());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Duplicate Import");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problem.setProperty("existingImportId", exception.getExistingImportId());

        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException exception) {
        log.debug("Domain validation error: {}", exception.getMessage());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Validation Error");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        log.debug("Invalid argument: {}", exception.getMessage());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid Request");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problem;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        log.debug("File size exceeded: {}", exception.getMessage());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "File size exceeds the maximum allowed limit"
        );

        problem.setTitle("File Too Large");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problem;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception) {
        log.debug("Missing required parameter: {}", exception.getParameterName());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Required parameter '" + exception.getParameterName() + "' is missing"
        );

        problem.setTitle("Missing Parameter");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problem.setProperty("parameter", exception.getParameterName());

        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.debug("Invalid parameter type: {}", exception.getName());

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter '" + exception.getName() + "' has invalid type"
        );

        problem.setTitle("Invalid Parameter Type");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problem.setProperty("parameter", exception.getName());

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        log.error("Unexpected error: {}", exception.getMessage(), exception);

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("about:blank"));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        return problem;
    }
}
