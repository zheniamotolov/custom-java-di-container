package com.vizorgames.interview.exception;

public class ConstructorAmbiguityException extends RuntimeException
{
    public ConstructorAmbiguityException() {
        super();
    }
    public ConstructorAmbiguityException(String s) {
        super(s);
    }
    public ConstructorAmbiguityException(String s, Throwable throwable) {
        super(s, throwable);
    }
    public ConstructorAmbiguityException(Throwable throwable) {
        super(throwable);
    }
}
