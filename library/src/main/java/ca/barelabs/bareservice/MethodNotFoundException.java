package ca.barelabs.bareservice;

import javax.servlet.ServletException;


public class MethodNotFoundException extends ServletException {
	
    private static final long serialVersionUID = 8905673644786282354L;
    
    public MethodNotFoundException() {
        super();
    }

    public MethodNotFoundException(String message) {
        super(message);
    }
}
