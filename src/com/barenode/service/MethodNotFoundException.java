package com.barenode.service;

import javax.servlet.ServletException;


public class MethodNotFoundException extends ServletException
{
    private static final long serialVersionUID = 8905673644786282354L;
    
    public MethodNotFoundException()
    {
        super();
    }

    public MethodNotFoundException(String message)
    {
        super(message);
    }

    public MethodNotFoundException(String message, Throwable rootCause)
    {
        super(message, rootCause);
    }

    public MethodNotFoundException(Throwable rootCause)
    {
        super(rootCause);
    }
}
