package com.barenode.service.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.barenode.service.InvalidParameterException;
import com.barenode.service.internal.ParameterFactory.Parameter;


public class ParameterMapper
{
    public static final Pattern SECTION_PATTERN = Pattern.compile("^\\w+$");
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("^" + Pattern.quote("{") + "\\w+" + Pattern.quote("}") + "$");
    
    private final ParameterReference[] references;
    
    
    public ParameterMapper(String[] path, Class<?>[] types) throws InvalidParameterException
    {
        references = createReferences(path, types); 
    }
    
    private final ParameterReference[] createReferences(String[] path, Class<?>[] types) throws InvalidParameterException
    {
        Iterator<Class<?>> iterator = Arrays.asList(types).iterator();
        if(!iterator.hasNext() || !iterator.next().isAssignableFrom(HttpServletRequest.class))
            throw new InvalidParameterException("First parameter must be of type: " + HttpServletRequest.class.getName());
        if(!iterator.hasNext() || !iterator.next().isAssignableFrom(HttpServletResponse.class))
            throw new InvalidParameterException("Second parameter must be of type: " + HttpServletResponse.class.getName());

        ArrayList<ParameterReference> referenceList = new ArrayList<ParameterReference>();
        for(int i = 0; i < path.length; i++)
        {
            String section = path[i];
            if(SECTION_PATTERN.matcher(section).matches())
                continue;
            else if(PARAMETER_PATTERN.matcher(section).matches())
            {
                String name = section.substring(1, section.length() - 1);
                if(!iterator.hasNext())
                    throw new InvalidParameterException(name + " doesn't have a matching parameter type!");
                
                path[i] = null;
                Parameter parameter = ParameterFactory.create(iterator.next());
                referenceList.add(new ParameterReference(i, name, parameter));
            }
            else
                throw new InvalidParameterException("/service/path must contain only alpha-numeric characters and braces {}!");
        }
        if(iterator.hasNext())
            throw new InvalidParameterException("Too many declared method parameters!");
        
        return referenceList.toArray(new ParameterReference[referenceList.size()]);
    }

    public Object[] map(HttpServletRequest request, HttpServletResponse response, String[] requestPath)
    {
        Object[] arguments = new Object[references.length + 2];
        arguments[0] = request;
        arguments[1] = response;
        for(int i = 0; i < references.length; i++)
            arguments[i + 2] = references[i].create(requestPath);
        return arguments;
    }
}