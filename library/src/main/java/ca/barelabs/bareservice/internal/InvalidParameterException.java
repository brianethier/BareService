package ca.barelabs.bareservice.internal;

import javax.servlet.ServletException;


public class InvalidParameterException extends ServletException {
	
    private static final long serialVersionUID = 8905673644786282354L;
    
    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public InvalidParameterException(Throwable rootCause) {
        super(rootCause);
    }
}
