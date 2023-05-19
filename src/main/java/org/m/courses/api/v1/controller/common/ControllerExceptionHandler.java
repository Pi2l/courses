package org.m.courses.api.v1.controller.common;

import org.m.courses.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handler(RuntimeException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("cause", exception.getMessage());
        return errorMap;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, String> handler(AccessDeniedException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("cause", exception.getMessage());
        return errorMap;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handler(IllegalArgumentException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("cause", exception.getMessage());
        return errorMap;
    }

    @ExceptionHandler(UserNotInGroupException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> handler(UserNotInGroupException exception) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("cause", exception.getMessage());
        return errorMap;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Object> handlerValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        for (FieldError field : exception.getFieldErrors()) {
            validationErrorsMap.put( field.getField(), field.getDefaultMessage() );
        }

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(UniqueFieldViolationException.class)
    ResponseEntity<Object> handlerValidationException(UniqueFieldViolationException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        validationErrorsMap.put(exception.getField(), exception.getMessage());

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> handlerValidationException(ConstraintViolationException exception) {
        Map<String, Object> map = getStringObjectMap(exception.getConstraintViolations());
        return new ResponseEntity<>(map, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(IllegalFilteringOperationException.class)
    ResponseEntity<Object> handlerValidationException(IllegalFilteringOperationException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        validationErrorsMap.put("filtering", exception.getMessage());

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserUnauthenticatedException.class)
    ResponseEntity<Object> handlerValidationException(UserUnauthenticatedException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        validationErrorsMap.put("", exception.getMessage());

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Object> handlerValidationException(BadCredentialsException exception) {
        Map<String, String> validationErrorsMap = new HashMap<>();

        validationErrorsMap.put("", exception.getMessage());

        return new ResponseEntity<>(validationErrorsMap, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PatchFieldValidationException.class)
    ResponseEntity<Object> handlerPatchValidationException(PatchFieldValidationException exception) {
        Set<? extends ConstraintViolation<?>> validationErrorsSet = exception.getValidationViolations();
        Map<String, Object> validationErrorsMap = getStringObjectMap(validationErrorsSet);
        return new ResponseEntity<>(validationErrorsMap, HttpStatus.BAD_REQUEST);
    }

    private Map<String, Object> getStringObjectMap(Set<? extends ConstraintViolation<?>> validationErrorsSet) {
        Map<String, Object> validationErrorsMap = new HashMap<>();

        for (ConstraintViolation<?> validationError : validationErrorsSet) {
            validationErrorsMap.put( validationError.getPropertyPath().toString(), validationError.getMessage());
        }
        return validationErrorsMap;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    String handlerAll(Throwable exception) {
        return exception.getMessage();
    }
}
