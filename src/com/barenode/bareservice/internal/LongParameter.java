package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class LongParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Long.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Long!", e);
        }
    }
}
