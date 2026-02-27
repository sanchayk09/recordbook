package com.urviclean.recordbook.exception;

public class InvalidInputException extends RuntimeException {

    private String field;

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
