package com.barenode.service.internal;


import com.barenode.service.internal.ParameterFactory.Parameter;


public class ByteParameter implements Parameter
{
    @Override
    public Object toObject(String value)
    {
        try
        {
            return Byte.valueOf(value);
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Byte!", e);
        }
    }
}