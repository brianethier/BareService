package com.barenode.bareservice.internal;


import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class StringParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        return value;
    }
}
