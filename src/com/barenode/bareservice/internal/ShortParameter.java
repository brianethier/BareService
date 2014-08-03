package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class ShortParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Short.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Short!", e);
        }
    }
}
