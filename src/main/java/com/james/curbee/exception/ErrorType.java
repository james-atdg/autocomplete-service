package com.james.curbee.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorType {
    NOT_FOUND(HttpStatus.NOT_FOUND.value()),
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value()),
	I_AM_A_TEAPOT(HttpStatus.I_AM_A_TEAPOT.value());

    @Getter
    private final int status;

    ErrorType(int status) {
        this.status = status;
    }

}
