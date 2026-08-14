package com.backend.couriersyncfeat4.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCodes errorCode;
    private final HttpStatus httpStatus;

    public ApplicationException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.valueOf(errorCode.getStatusCode());
    }

    public ApplicationException(ErrorCodes errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
