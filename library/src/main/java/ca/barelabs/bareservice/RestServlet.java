/*
 * Copyright 2013 Brian Ethier
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ca.barelabs.bareservice;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.barelabs.bareservice.annotation.ConnectionInstance;
import ca.barelabs.bareservice.annotation.DELETE;
import ca.barelabs.bareservice.annotation.GET;
import ca.barelabs.bareservice.annotation.GUEST;
import ca.barelabs.bareservice.annotation.HEAD;
import ca.barelabs.bareservice.annotation.OPTIONS;
import ca.barelabs.bareservice.annotation.POST;
import ca.barelabs.bareservice.annotation.PUT;
import ca.barelabs.bareservice.annotation.TRACE;
import ca.barelabs.bareservice.internal.InvalidClassAnnotationException;
import ca.barelabs.bareservice.internal.InvalidParameterException;
import ca.barelabs.bareservice.internal.InvalidServiceMethodException;
import ca.barelabs.bareservice.internal.PathUtils;
import ca.barelabs.bareservice.internal.ServiceMethod;
import ca.barelabs.bareservice.internal.ServiceMethodComparator;


@SuppressWarnings("serial")
public class RestServlet extends HttpServlet {

    private Class<? extends AbstractConnection> mConnectionClss;
    private ServiceMethod[] mGet;
    private ServiceMethod[] mPost;
    private ServiceMethod[] mPut;
    private ServiceMethod[] mDelete;
    private ServiceMethod[] mHead;
    private ServiceMethod[] mOptions;
    private ServiceMethod[] mTrace;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        mConnectionClss = loadConnectionInstanceClass();
        mGet = loadMethods(GET.class);
        mPost = loadMethods(POST.class);
        mPut = loadMethods(PUT.class);
        mDelete = loadMethods(DELETE.class);
        mHead = loadMethods(HEAD.class);
        mOptions = loadMethods(OPTIONS.class);
        mTrace = loadMethods(TRACE.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mGet, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mPost, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mPut, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mDelete, request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mHead, request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mOptions, request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(mTrace, request, response);
    }

    private final void processRequest(ServiceMethod[] methods, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        AbstractConnection connection = createConnectionInstance();
		connection.init(request, response);
        String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            String[] path = PathUtils.splitPath(pathInfo);
            ServiceMethod method = findMatch(methods, path);
            if (method == null) {
                throw new MethodNotFoundException(request.getRequestURI());
            }
            connection.onConnected(method.isGuestAccess());
            Object[] arguments = method.createArguments(connection, path);
            Object value = method.getMethod().invoke(this, arguments);
            if (method.isValueReturned()) {
            	connection.onValueReturned(value);
            }
        } catch (IllegalArgumentException e) {
            String message = String.format("'%s' contains invalid sections for the declared parameters!", pathInfo);
            throw new MethodInvocationException(message, e);
        } catch (IllegalAccessException e) {
            String message = String.format("'%s' maps to a method that can't be accessed! Make sure all service methods are declared public.", pathInfo);
            throw new MethodInvocationException(message, e);
        } catch (InvocationTargetException e) {
            if (response.isCommitted() || !connection.onServiceException(e.getTargetException())) {
	            String message = String.format("Method mapped to the path '%s' threw an exception while being invoked!", pathInfo);
	            throw new MethodInvocationException(message, e.getTargetException());
            }
        } catch (Exception e) {
            if (response.isCommitted() || !connection.onServiceException(e)) {
                String message = String.format("Method mapped to the path '%s' threw an exception while being invoked!", pathInfo);
                throw new MethodInvocationException(message, e);
            }
        } finally {
            connection.close();
        }
    }
    
    private final ServiceMethod findMatch(ServiceMethod[] methods, String[] path) {
        for (ServiceMethod method : methods)
            if (method.matches(path))
                return method;

        return null;
    }
    
    private final AbstractConnection createConnectionInstance() throws ServletException {
        if (mConnectionClss == null) {
            String message = String.format("Invalid class annotation in class %s. Make sure to add the following annotation to your class: ", getClass().getName(), ConnectionInstance.class.getName());
            throw new InvalidClassAnnotationException(message);
        }
        try {
            return mConnectionClss.newInstance();
        } catch (IllegalAccessException e) {
            String message = String.format("Invalid class annotation in class %s. Make sure following class has a public default constructor: ", getClass().getName(), mConnectionClss.getName());
            throw new InvalidClassAnnotationException(message);
        } catch (InstantiationException e) {
            String message = String.format("Invalid class annotation in class %s. Make sure following class can be instantiated: ", getClass().getName(), mConnectionClss.getName());
            throw new InvalidClassAnnotationException(message);
        }
    }
    
    private final Class<? extends AbstractConnection> loadConnectionInstanceClass() throws ServletException {
        ConnectionInstance annotation = getClass().getAnnotation(ConnectionInstance.class);
        return  annotation == null ? null : annotation.value();
    }

    private final ServiceMethod[] loadMethods(Class<? extends Annotation> annotationClass) throws ServletException {
        List<ServiceMethod> methods = new ArrayList<ServiceMethod>();
        for (Method declaredMethod : getClass().getDeclaredMethods()) {
            Annotation annotation = declaredMethod.getAnnotation(annotationClass);
            if (annotation != null) {
            	if (!Modifier.isPublic(declaredMethod.getModifiers())) {
                	String message = String.format("Invalid access modifier, must be 'public' for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName());
                    throw new InvalidServiceMethodException(message);
            	}
                try {
                    Annotation guestAnnotation = declaredMethod.getAnnotation(GUEST.class);
                    ServiceMethod method = new ServiceMethod(declaredMethod, annotation, guestAnnotation != null);
                    if (methods.contains(method)) {
                        ServiceMethod duplicate = methods.get(methods.indexOf(method));
                        String message = String.format("Duplicate annotation on \"%s(...)\" same as \"%s(...)\"!", declaredMethod.getName(), duplicate.getName());
                        throw new InvalidServiceMethodException(message);
                    }
                    methods.add(method);
                }
                catch (InvalidParameterException e) {
                	String message = String.format("Invalid parameter for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName());
                    throw new InvalidServiceMethodException(message, e);
                }
            }
        }
        Collections.sort(methods, new ServiceMethodComparator());
        return methods.toArray(new ServiceMethod[methods.size()]);
    }
}