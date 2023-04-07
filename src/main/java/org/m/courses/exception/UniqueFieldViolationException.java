package org.m.courses.exception;


public class UniqueFieldViolationException extends RuntimeException {

    private String field;

    private final String message = "must be unique";

    public UniqueFieldViolationException(String field) {
        this.field = field;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
