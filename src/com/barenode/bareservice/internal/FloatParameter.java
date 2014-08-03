package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class FloatParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Float.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Float!", e);
        }
    }
}
