package org.m.courses.exception;

import org.m.courses.api.v1.controller.user.UserRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

public class PatchFieldValidationException extends ConstraintViolationException {

    private Set<? extends ConstraintViolation<?>> validationViolations;

    public PatchFieldValidationException(String message, Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(message, constraintViolations);
    }

    public PatchFieldValidationException(Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(constraintViolations);
        this.validationViolations = constraintViolations;
    }

    public Set<? extends ConstraintViolation<?>> getValidationViolations() {
        return validationViolations;
    }

    public void setValidationViolations(Set<? extends ConstraintViolation<?>> validationViolations) {
        this.validationViolations = validationViolations;
    }
}
