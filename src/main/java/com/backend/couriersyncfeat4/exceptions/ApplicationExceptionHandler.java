package com.backend.couriersyncfeat4.exceptions;

import com.backend.couriersyncfeat4.dto.output.ApiErrorResponse;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> handleApplicationException(
            final ApplicationException exception, final HttpServletRequest request
    ) {
        var guid = UUID.randomUUID().toString();
        log.error(
                    String.format("Error GUID=%s; error message: %s", guid, exception.getMessage()), 
            exception
        );
        var response = new ApiErrorResponse(
                guid,
                exception.getErrorCode().getCode(),
                exception.getMessage(),
                exception.getHttpStatus().value(),
                exception.getHttpStatus().name(),
                request.getRequestURI(),
                request.getMethod(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, exception.getHttpStatus());
    }

    @GraphQlExceptionHandler
    public GraphQLError handleGraphQlApplicationException(final ApplicationException exception) {
        log.error("GraphQL error code={}; message: {}", exception.getErrorCode().getCode(),
                exception.getMessage());
        return GraphqlErrorBuilder.newError()
                .message(exception.getMessage())
                .errorType(toErrorType(exception.getHttpStatus()))
                .extensions(Map.of(
                        "code", exception.getErrorCode().getCode(),
                        "status", exception.getHttpStatus().value()))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleAccessDeniedException(final AccessDeniedException exception) {
        log.warn("Forbidden: {}", exception.getMessage());
        return GraphqlErrorBuilder.newError()
                .message("Forbidden")
                .errorType(ErrorType.FORBIDDEN)
                .build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException exception,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request
    ) {
        var guid = UUID.randomUUID().toString();
        var message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String uri = "";
        String method = "";
        if (request instanceof ServletWebRequest servletWebRequest) {
            uri = servletWebRequest.getRequest().getRequestURI();
            method = servletWebRequest.getRequest().getMethod();
        }

        var response = new ApiErrorResponse(
                guid,
                ErrorCodes.INVALID_INPUT.getCode(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                uri,
                method,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknownException(
        final Exception exception, final HttpServletRequest request
    ) {
        var guid = UUID.randomUUID().toString();
        log.error(
            String.format("Error GUID=%s; error message: %s", guid, exception.getMessage()), 
            exception
        );
        var response = new ApiErrorResponse(
                guid,
                ErrorCodes.INTERNAL_ERROR.getCode(),
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                request.getRequestURI(),
                request.getMethod(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ErrorType toErrorType(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorType.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorType.UNAUTHORIZED;
            case FORBIDDEN -> ErrorType.FORBIDDEN;
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }

}
