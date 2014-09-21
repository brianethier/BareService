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
package com.barenode.bareservice;

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

import com.barenode.bareservice.annotation.DELETE;
import com.barenode.bareservice.annotation.GET;
import com.barenode.bareservice.annotation.HEAD;
import com.barenode.bareservice.annotation.OPTIONS;
import com.barenode.bareservice.annotation.POST;
import com.barenode.bareservice.annotation.PUT;
import com.barenode.bareservice.annotation.TRACE;
import com.barenode.bareservice.annotation.ValueReturnedCallback;
import com.barenode.bareservice.internal.InvalidClassAnnotationException;
import com.barenode.bareservice.internal.InvalidParameterException;
import com.barenode.bareservice.internal.InvalidServiceMethodException;
import com.barenode.bareservice.internal.PathUtils;
import com.barenode.bareservice.internal.ServiceMethod;
import com.barenode.bareservice.internal.ServiceMethodComparator;


@SuppressWarnings("serial")
public class RestServlet extends HttpServlet {
    
    public interface OnValueReturnedCallback {
        public void onValueReturned(HttpServletResponse response, Object value) throws IOException;
    }

    private ServiceMethod[] mGet;
    private ServiceMethod[] mPost;
    private ServiceMethod[] mPut;
    private ServiceMethod[] mDelete;
    private ServiceMethod[] mHead;
    private ServiceMethod[] mOptions;
    private ServiceMethod[] mTrace;
    private OnValueReturnedCallback mValueReturnedCallback;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        mGet = loadMethods(GET.class);
        mPost = loadMethods(POST.class);
        mPut = loadMethods(PUT.class);
        mDelete = loadMethods(DELETE.class);
        mHead = loadMethods(HEAD.class);
        mOptions = loadMethods(OPTIONS.class);
        mTrace = loadMethods(TRACE.class);
        mValueReturnedCallback = loadValueReturnedCallback();
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
    
    protected void onValueReturned(HttpServletResponse response, Object value) throws IOException {
        if(mValueReturnedCallback != null) {
            mValueReturnedCallback.onValueReturned(response, value);
        }
    }

    private final void processRequest(ServiceMethod[] methods, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            String[] path = PathUtils.splitPath(pathInfo);
            ServiceMethod method = findMatch(methods, path);
            if(method == null) {
            	String message = String.format("'%s' didn't match any of the declared service methods!", pathInfo);
                throw new MethodNotFoundException(message);
            }
            Object value = method.invoke(this, request, response, path);
            if(method.isValueReturned()) {
                onValueReturned(response, value);
            }
        }
        catch(IllegalArgumentException e) {
            String message = String.format("'%s' contains invalid sections for the declared parameters!", pathInfo);
            throw new MethodInvocationException(message, e);
        }
        catch(IllegalAccessException e) {
            String message = String.format("'%s' maps to a method that can't be accessed! Make sure all service methods are declared public.", pathInfo);
            throw new MethodInvocationException(message, e);
        }
        catch(InvocationTargetException e) {
            if(e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
            String message = String.format("Method mapped to the path '%s' threw an exception while being invoked!", pathInfo);
            throw new MethodInvocationException(message, e.getTargetException());
        }
    }
    
    private final ServiceMethod findMatch(ServiceMethod[] methods, String[] path) {
        for(ServiceMethod method : methods)
            if(method.matches(path))
                return method;

        return null;
    }
    
    private final OnValueReturnedCallback loadValueReturnedCallback() throws InvalidClassAnnotationException {
        try {
            ValueReturnedCallback annotation = getClass().getAnnotation(ValueReturnedCallback.class);
            return annotation == null || annotation.value() == null ? null : annotation.value().newInstance();
        } catch (InstantiationException e) {
            String message = String.format("Invalid class annotation in class %s. Make sure the provided value is a Class that implements OnValueReturnedCallback!", getClass().getName());
            throw new InvalidClassAnnotationException(message, e);
        } catch (IllegalAccessException e) {
            String message = String.format("Invalid class annotation in class %s. Make sure the provided value is a Class that implements OnValueReturnedCallback!", getClass().getName());
            throw new InvalidClassAnnotationException(message, e);
        }
    }

    private final ServiceMethod[] loadMethods(Class<? extends Annotation> annotationClass) throws ServletException {
        List<ServiceMethod> methods = new ArrayList<ServiceMethod>();
        for(Method declaredMethod : getClass().getDeclaredMethods()) {
            Annotation annotation = declaredMethod.getAnnotation(annotationClass);
            if(annotation != null) {
            	if(!Modifier.isPublic(declaredMethod.getModifiers())) {
                	String message = String.format("Invalid access modifier, must be 'public' for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName());
                    throw new InvalidServiceMethodException(message);
            	}
                try {
                    ServiceMethod method = new ServiceMethod(declaredMethod, annotation);
                    if(methods.contains(method)) {
                        ServiceMethod duplicate = methods.get(methods.indexOf(method));
                        String message = String.format("Duplicate annotation on \"%s(...)\" same as \"%s(...)\"!", declaredMethod.getName(), duplicate.getName());
                        throw new InvalidServiceMethodException(message);
                    }
                    methods.add(method);
                }
                catch(InvalidParameterException e) {
                	String message = String.format("Invalid parameter for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName());
                    throw new InvalidServiceMethodException(message, e);
                }
            }
        }
        Collections.sort(methods, new ServiceMethodComparator());
        return methods.toArray(new ServiceMethod[methods.size()]);
    }
}