package com.james.curbee.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.I_AM_A_TEAPOT, reason = "There are no matching suggestions")
public class NoSuggestionsException extends RuntimeException{

}
