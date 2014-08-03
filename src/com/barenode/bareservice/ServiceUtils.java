package com.barenode.bareservice;

import java.lang.annotation.Annotation;

import com.barenode.bareservice.annotation.DELETE;
import com.barenode.bareservice.annotation.GET;
import com.barenode.bareservice.annotation.HEAD;
import com.barenode.bareservice.annotation.OPTIONS;
import com.barenode.bareservice.annotation.POST;
import com.barenode.bareservice.annotation.PUT;
import com.barenode.bareservice.annotation.TRACE;


public class ServiceUtils {
	
    public static final String PATH_SEPARATOR = "/";

    
    private ServiceUtils() {}

    
    public static String[] splitPath(String path) {
        String pathValue = path != null && path.startsWith(PATH_SEPARATOR) ? path.substring(1) : path;
        if(pathValue == null || pathValue.length() == 0)
            return new String[0];

        return pathValue.split(PATH_SEPARATOR);
    }
    
    public static String joinPath(String[] path) {
        StringBuilder sb = new StringBuilder(PATH_SEPARATOR);
        if(path != null) {
            for(String section : path)
                sb.append(section + PATH_SEPARATOR);
        }
        return sb.toString();
    }

    public static String getPath(Annotation annotation) {
        if(annotation instanceof GET)
            return ((GET) annotation).value();
        if(annotation instanceof POST)
            return ((POST) annotation).value();
        if(annotation instanceof PUT)
            return ((PUT) annotation).value();
        if(annotation instanceof DELETE)
            return ((DELETE) annotation).value();
        if(annotation instanceof HEAD)
            return ((HEAD) annotation).value();
        if(annotation instanceof OPTIONS)
            return ((OPTIONS) annotation).value();
        if(annotation instanceof TRACE)
            return ((TRACE) annotation).value();

        throw new IllegalArgumentException("Invalid Annotation type!");
    }

    public static String[] getSplitPath(Annotation annotation) {
        return splitPath(getPath(annotation));
    }
}