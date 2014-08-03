package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class IntegerParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Integer!", e);
        }
    }
}
