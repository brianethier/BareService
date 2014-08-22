package com.barenode.bareservice.internal;

import javax.servlet.ServletException;


public class InvalidServiceMethodException extends ServletException {
	
    private static final long serialVersionUID = 8905673644786282354L;
    
    public InvalidServiceMethodException() {
        super();
    }

    public InvalidServiceMethodException(String message) {
        super(message);
    }

    public InvalidServiceMethodException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public InvalidServiceMethodException(Throwable rootCause) {
        super(rootCause);
    }
}
