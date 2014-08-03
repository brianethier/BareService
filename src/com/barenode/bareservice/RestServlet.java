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
import com.barenode.bareservice.internal.InvalidParameterException;
import com.barenode.bareservice.internal.MethodNotFoundException;
import com.barenode.bareservice.internal.ServiceMethod;
import com.barenode.bareservice.internal.ServiceMethodComparator;


@SuppressWarnings("serial")
public class RestServlet extends HttpServlet {

    private ServiceMethod[] get;
    private ServiceMethod[] post;
    private ServiceMethod[] put;
    private ServiceMethod[] delete;
    private ServiceMethod[] head;
    private ServiceMethod[] options;
    private ServiceMethod[] trace;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        get = loadMethods(GET.class);
        post = loadMethods(POST.class);
        put = loadMethods(PUT.class);
        delete = loadMethods(DELETE.class);
        head = loadMethods(HEAD.class);
        options = loadMethods(OPTIONS.class);
        trace = loadMethods(TRACE.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(get, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(post, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(put, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(delete, request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(head, request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(options, request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(trace, request, response);
    }

    private final void processRequest(ServiceMethod[] methods, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String pathInfo = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            String[] path = ServiceUtils.splitPath(pathInfo);
            ServiceMethod method = findMatch(methods, path);
            method.invoke(this, request, response, path);
        }
        catch(MethodNotFoundException e) {
            String message = String.format("'%s' didn't match any of the declared service methods!", pathInfo);
            log(message, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
        catch(IllegalArgumentException e) {
            String message = String.format("'%s' contains invalid sections for the declared parameters!", pathInfo);
            log(message, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
        catch(IllegalAccessException e) {
            String message = String.format("'%s' maps to a method that can't be accessed! Make sure all service methods are declared public.", pathInfo);
            log(message, e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
        }
        catch(InvocationTargetException e) {
            String message = String.format("'%s' maps to a method that could not be invoked!", pathInfo);
            log(message, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
    }
    
    private final ServiceMethod findMatch(ServiceMethod[] methods, String[] path) throws MethodNotFoundException {
        for(ServiceMethod method : methods)
            if(method.matches(path))
                return method;

        throw new MethodNotFoundException();
    }

    private final ServiceMethod[] loadMethods(Class<? extends Annotation> annotationClass) {
        List<ServiceMethod> methods = new ArrayList<ServiceMethod>();
        for(Method declaredMethod : getClass().getDeclaredMethods()) {
            Annotation annotation = declaredMethod.getAnnotation(annotationClass);
            if(annotation != null) {
                try {
                    ServiceMethod method = new ServiceMethod(declaredMethod, annotation);
                    if(methods.contains(method)) {
                        ServiceMethod duplicate = methods.get(methods.indexOf(method));
                        log(String.format("Invalid service annotation! Duplicate annotation on \"%s(...)\" same as \"%s(...)\"!", declaredMethod.getName(), duplicate.getName()));
                    }
                    else 
                        methods.add(method);
                }
                catch(InvalidParameterException e) {
                    log(String.format("Invalid service annotation for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName()), e);
                }
            }
        }
        Collections.sort(methods, new ServiceMethodComparator());
        return methods.toArray(new ServiceMethod[methods.size()]);
    }
}