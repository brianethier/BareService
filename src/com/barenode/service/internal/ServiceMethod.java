package com.barenode.service.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.barenode.service.HttpService;
import com.barenode.service.InvalidParameterException;


public class ServiceMethod
{
    private final Method method;
    private final Annotation annotation;
    private final String path[];
    private final ParameterMapper parameterMapper;

    
    public ServiceMethod(Method method, Annotation annotation) throws InvalidParameterException
    {
        this.method = method;
        this.annotation = annotation;
        this.path = ServiceUtils.getPath(annotation);
        this.parameterMapper = new ParameterMapper(path, method.getParameterTypes());
    }

    
    public Annotation getAnnotation()
    {
        return annotation;
    }

    public String[] getPath()
    {
        return path;
    }

    public boolean isPathEmpty()
    {
        return path.length == 0;
    }
    
    public String getName()
    {
        return method.getName();
    }

    public boolean matches(String[] requestPath)
    {
        if(requestPath == null || path.length != requestPath.length)
            return false;

        for(int i = 0; i < path.length; i++)
        {
            if(path[i] == null)
                continue;
            if(!path[i].equals(requestPath[i]))
                return false;
        }
        return true;
    }

    public void invoke(HttpService service, HttpServletRequest request, HttpServletResponse response, String[] requestPath) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Object[] arguments = parameterMapper.map(request, response, requestPath);
        method.invoke(service, arguments);
    }
    
    @Override
    public int hashCode()
    {
        return Arrays.asList(path).hashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if(object == null)
            return false;
        if(object == this)
            return true;
        if(object instanceof ServiceMethod)
        {
            ServiceMethod method = (ServiceMethod) object;
            return Arrays.equals(path, method.path);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}