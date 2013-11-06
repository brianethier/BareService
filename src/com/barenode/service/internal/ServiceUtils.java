package com.barenode.service.internal;

import java.lang.annotation.Annotation;

import com.barenode.service.annotation.DELETE;
import com.barenode.service.annotation.GET;
import com.barenode.service.annotation.HEAD;
import com.barenode.service.annotation.OPTIONS;
import com.barenode.service.annotation.POST;
import com.barenode.service.annotation.PUT;
import com.barenode.service.annotation.TRACE;


public class ServiceUtils
{
    public static final String PATH_SEPARATOR = "/";

    
    private ServiceUtils() {}

    
    public static String[] splitPath(String path)
    {       
        String pathValue = path != null && path.startsWith(PATH_SEPARATOR) ? path.substring(1) : path;
        if(pathValue == null || pathValue.length() == 0)
            return new String[0];

        return pathValue.split(PATH_SEPARATOR);
    }
    
    public static String joinPath(String[] path)
    {
        StringBuilder sb = new StringBuilder(PATH_SEPARATOR);
        if(path != null)
        {
            for(String section : path)
                sb.append(section + PATH_SEPARATOR);
        }
        return sb.toString();
    }

    public static String[] getPath(Annotation annotation)
    {
        if(annotation instanceof GET)
            return splitPath(((GET) annotation).value());
        if(annotation instanceof POST)
            return splitPath(((POST) annotation).value());
        if(annotation instanceof PUT)
            return splitPath(((PUT) annotation).value());
        if(annotation instanceof DELETE)
            return splitPath(((DELETE) annotation).value());
        if(annotation instanceof HEAD)
            return splitPath(((HEAD) annotation).value());
        if(annotation instanceof OPTIONS)
            return splitPath(((OPTIONS) annotation).value());
        if(annotation instanceof TRACE)
            return splitPath(((TRACE) annotation).value());

        throw new IllegalArgumentException("Invalid Annotation type!");
    }
}