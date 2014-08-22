package com.barenode.bareservice.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.barenode.bareservice.RestServlet;
import com.barenode.bareservice.internal.ParameterFactory.Parameter;


public class ServiceMethod {
	
    public static final Pattern SECTION_PATTERN = Pattern.compile("^\\w+$");
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("^" + Pattern.quote("{") + "\\w+" + Pattern.quote("}") + "$");

    private final Method method;
    private final Annotation annotation;
    private final String path[];
    private final ParameterReference[] references;

    
    public ServiceMethod(Method method, Annotation annotation) throws InvalidParameterException {
        this.method = method;
        this.annotation = annotation;
        this.path = PathUtils.getSplitPath(annotation);
        this.references = createReferences(path, method.getParameterTypes()); 
    }

    
    public Annotation getAnnotation() {
        return annotation;
    }

    public String[] getPath() {
        return path;
    }

    public boolean isPathEmpty() {
        return path.length == 0;
    }
    
    public String getName() {
        return method.getName();
    }

    public boolean matches(String[] requestPath) {
        if(requestPath == null || path.length != requestPath.length)
            return false;

        for(int i = 0; i < path.length; i++) {
            if(path[i] == null)
                continue;
            if(!path[i].equals(requestPath[i]))
                return false;
        }
        return true;
    }

    public void invoke(RestServlet service, HttpServletRequest request, HttpServletResponse response, String[] requestPath) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object[] arguments = new Object[references.length + 2];
        arguments[0] = request;
        arguments[1] = response;
        for(int i = 0; i < references.length; i++)
            arguments[i + 2] = references[i].toObject(requestPath);
        method.invoke(service, arguments);
    }
    
    @Override
    public int hashCode() {
        return Arrays.asList(path).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object == null)
            return false;
        if(object == this)
            return true;
        if(object instanceof ServiceMethod) {
            ServiceMethod method = (ServiceMethod) object;
            return Arrays.equals(path, method.path);
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    private final ParameterReference[] createReferences(String[] path, Class<?>[] types) throws InvalidParameterException {
        Iterator<Class<?>> iterator = Arrays.asList(types).iterator();
        if(!iterator.hasNext() || !iterator.next().isAssignableFrom(HttpServletRequest.class))
            throw new InvalidParameterException("First parameter must be of type: " + HttpServletRequest.class.getName());
        if(!iterator.hasNext() || !iterator.next().isAssignableFrom(HttpServletResponse.class))
            throw new InvalidParameterException("Second parameter must be of type: " + HttpServletResponse.class.getName());

        ArrayList<ParameterReference> referenceList = new ArrayList<ParameterReference>();
        for(int i = 0; i < path.length; i++) {
            String section = path[i];
            if(SECTION_PATTERN.matcher(section).matches())
                continue;
            else if(PARAMETER_PATTERN.matcher(section).matches()) {
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
}