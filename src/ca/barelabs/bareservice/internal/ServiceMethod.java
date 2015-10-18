package ca.barelabs.bareservice.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.barelabs.bareservice.RestServlet;
import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class ServiceMethod {
	
    public static final Pattern SECTION_PATTERN = Pattern.compile("^\\w+$");
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("^" + Pattern.quote("{") + "\\w+" + Pattern.quote("}") + "$");

    private final Method mMethod;
    private final boolean mValueReturned;
    private final Annotation mAnnotation;
    private final String mPath[];
    private final ParameterReference[] mReferences;

    
    public ServiceMethod(Method method, Annotation annotation) throws InvalidParameterException {
        mMethod = method;
        mValueReturned = !method.getReturnType().equals(Void.TYPE);
        mAnnotation = annotation;
        mPath = PathUtils.getSplitPath(annotation);
        mReferences = createReferences(mPath, method.getParameterTypes()); 
    }

    
    public boolean isValueReturned() {
        return mValueReturned;
    }
    
    public Annotation getAnnotation() {
        return mAnnotation;
    }

    public String[] getPath() {
        return mPath;
    }

    public boolean isPathEmpty() {
        return mPath.length == 0;
    }
    
    public String getName() {
        return mMethod.getName();
    }

    public boolean matches(String[] requestPath) {
        if(requestPath == null || mPath.length != requestPath.length)
            return false;

        for(int i = 0; i < mPath.length; i++) {
            if(mPath[i] == null)
                continue;
            if(!mPath[i].equals(requestPath[i]))
                return false;
        }
        return true;
    }

    public Object invoke(RestServlet service, HttpServletRequest request, HttpServletResponse response, String[] requestPath) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object[] arguments = new Object[mReferences.length + 2];
        arguments[0] = request;
        arguments[1] = response;
        for(int i = 0; i < mReferences.length; i++)
            arguments[i + 2] = mReferences[i].toObject(requestPath);
        return mMethod.invoke(service, arguments);
    }
    
    @Override
    public int hashCode() {
        return Arrays.asList(mPath).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object == null)
            return false;
        if(object == this)
            return true;
        if(object instanceof ServiceMethod) {
            ServiceMethod method = (ServiceMethod) object;
            return Arrays.equals(mPath, method.mPath);
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