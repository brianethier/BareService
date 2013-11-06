package com.barenode.service.internal;


import com.barenode.service.internal.ParameterFactory.Parameter;


public class DoubleParameter implements Parameter
{
    @Override
    public Object toObject(String value)
    {
        try
        {
            return Double.valueOf(value);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Double!", e);
        }
    }
}
