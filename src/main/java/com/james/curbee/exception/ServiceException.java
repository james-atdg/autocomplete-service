package com.james.curbee.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 3317018509399315692L;
	private final ErrorType type;
    private final String message;
    private final Object[] params;

    public ServiceException(ErrorType type, String message, Object... params) {
        super(message);
        this.type = type;
        this.message = message;
        this.params = params;
    }

}
