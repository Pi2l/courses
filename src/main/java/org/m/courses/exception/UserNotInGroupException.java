package org.m.courses.exception;

public class UserNotInGroupException extends RuntimeException {

    public UserNotInGroupException() {
        super("user has to be in group");
    }

    public UserNotInGroupException(String message) {
        super(message);
    }
}
