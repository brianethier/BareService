package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class BooleanParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        if(value != null && value.length() == 1 && value.equals("1"))
            return Boolean.valueOf(true);

        return Boolean.valueOf(value);
    }
}
