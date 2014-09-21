package com.barenode.bareservice.internal;

import javax.servlet.ServletException;


public class InvalidClassAnnotationException extends ServletException {
	
    private static final long serialVersionUID = 8905673644786282354L;
    
    public InvalidClassAnnotationException() {
        super();
    }

    public InvalidClassAnnotationException(String message) {
        super(message);
    }

    public InvalidClassAnnotationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public InvalidClassAnnotationException(Throwable rootCause) {
        super(rootCause);
    }
}
