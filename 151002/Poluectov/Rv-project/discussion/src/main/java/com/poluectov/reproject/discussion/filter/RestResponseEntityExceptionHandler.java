package com.poluectov.reproject.discussion.filter;


import com.poluectov.reproject.discussion.model.RestError;
import com.poluectov.reproject.discussion.repository.exception.EntityNotFoundException;
import com.poluectov.reproject.discussion.repository.exception.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = {IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        RestError restError = RestError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(restError.getStatus()).body(restError);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        RestError restError = RestError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(restError.getStatus()).body(restError);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<RestError> handleEntityNotFound(
            RuntimeException ex, WebRequest request) {
        RestError error = RestError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(ex.getMessage())
                .build();


        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        log.debug(
                "Constraint violation exception encountered: {}",
                ex.getConstraintViolations(),
                ex
        );

        List<ValidationError> errors = buildValidationErrors(
                ex.getConstraintViolations()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
    }
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    protected ResponseEntity<Object> handleSQLException(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Database error occurred";

        RestError restError = RestError.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(bodyOfResponse)
                .build();

        return ResponseEntity.status(restError.getStatus()).body(restError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String bodyOfResponse = "Data integrity violation occurred";

        RestError restError = RestError.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(bodyOfResponse)
                .build();

        return ResponseEntity.status(restError.getStatus()).body(restError);
    }

    /**
     * Build list of ValidationError from set of ConstraintViolation
     *
     * @param violations Set<ConstraintViolation<?>> - Set
     * of parameterized ConstraintViolations
     * @return List<ValidationError> - list of validation errors
     */
    private List<ValidationError> buildValidationErrors(
            Set<ConstraintViolation<?>> violations) {
        return violations.
                stream().
                map(violation ->
                        ValidationError.builder()
                                .field(
                                        StreamSupport.stream(
                                                        violation.getPropertyPath().spliterator(), false)
                                                .reduce((first, second) -> second)
                                                .orElse(null)
                                                .toString()
                                )
                                .error(violation.getMessage())
                                .build())
                        .collect(toList());
    }
}
