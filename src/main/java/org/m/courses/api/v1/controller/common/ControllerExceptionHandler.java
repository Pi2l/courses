package org.m.courses.api.v1.controller.common;

import org.m.courses.exception.ItemNotFoundException;
import org.m.courses.exception.PatchFieldValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handler(RuntimeException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Object> handlerValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        for (FieldError field : exception.getFieldErrors()) {
            validationErrorsMap.put( field.getField(), field.getDefaultMessage() );
        }

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(PatchFieldValidationException.class)
    ResponseEntity<Object> handlerPatchValidationException(PatchFieldValidationException exception) {
        Set<? extends ConstraintViolation<?>> validationErrorsSet = exception.getValidationViolations();
        Map<String, Object> validationErrorsMap = new HashMap<>();

        for (ConstraintViolation<?> validationError : validationErrorsSet ) {
            validationErrorsMap.put( validationError.getPropertyPath().toString(), validationError.getMessage());
        }
        return new ResponseEntity<>(validationErrorsMap, HttpStatus.NOT_ACCEPTABLE);
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(Throwable.class)
    String handlerAll(Throwable exception) {
        return exception.getMessage();
    }
}
