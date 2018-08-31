package com.vizorgames.interview.exception;

public class NoSuitableConstructorException extends RuntimeException
{
    public NoSuitableConstructorException() {
        super();
    }
    public NoSuitableConstructorException(String s) {
        super(s);
    }
    public NoSuitableConstructorException(String s, Throwable throwable) {
        super(s, throwable);
    }
    public NoSuitableConstructorException(Throwable throwable) {
        super(throwable);
    }
}
