package com.barenode.service.internal;


import com.barenode.service.internal.ParameterFactory.Parameter;


public class StringParameter implements Parameter
{
    @Override
    public Object toObject(String value)
    {
        return value;
    }
}
