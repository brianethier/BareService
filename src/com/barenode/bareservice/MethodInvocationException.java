package com.barenode.bareservice;

import javax.servlet.ServletException;

public class MethodInvocationException extends ServletException {

	private static final long serialVersionUID = 5283931297600249967L;

    public MethodInvocationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

	public MethodInvocationException(Throwable rootCause) {
		super(rootCause);
	}
}
