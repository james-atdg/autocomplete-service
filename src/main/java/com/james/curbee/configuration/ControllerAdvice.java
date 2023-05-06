package com.james.curbee.configuration;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.james.curbee.exception.ErrorMessage;
import com.james.curbee.exception.ErrorType;
import com.james.curbee.exception.NoSuggestionsException;
import com.james.curbee.exception.ServiceException;
import com.james.curbee.util.LocaleMessageSource;

import static java.util.Optional.ofNullable;
import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    private final LocaleMessageSource messages;

    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<ErrorMessage> handleServiceException(ServiceException e) {
        log.error(e.getMessage());
        String message = messages.getMessage(e.getMessage(), e.getParams());
        ErrorMessage errorMessage = new ErrorMessage()
            .setType(e.getType())
            .setMessage(message)
            .setDateTime(LocalDateTime.now());
        return toResponseEntity(errorMessage);
    }
    
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorMessage> handleConstraintException(ConstraintViolationException e) {
        log.error(e.getMessage());
        String message = messages.getMessage(e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage()
            .setType(ErrorType.BAD_REQUEST)
            .setMessage(message)
            .setDateTime(LocalDateTime.now());
        return toResponseEntity(errorMessage);
    }

    private ResponseEntity<ErrorMessage> toResponseEntity(ErrorMessage errorMessage) {
        int status = ofNullable(errorMessage.getType())
            .map(ErrorType::getStatus)
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(status).body(errorMessage);
    }

}
