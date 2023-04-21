package org.m.courses.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("item not found with id = " + id);
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
