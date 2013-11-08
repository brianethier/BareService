package com.barenode.service;

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

import com.barenode.service.annotation.DELETE;
import com.barenode.service.annotation.GET;
import com.barenode.service.annotation.HEAD;
import com.barenode.service.annotation.OPTIONS;
import com.barenode.service.annotation.POST;
import com.barenode.service.annotation.PUT;
import com.barenode.service.annotation.TRACE;
import com.barenode.service.internal.ServiceMethod;
import com.barenode.service.internal.ServiceMethodComparator;
import com.barenode.service.internal.ServiceUtils;


public class HttpService extends HttpServlet
{
    private static final long serialVersionUID = 3170923521742307475L;

    private ServiceMethod[] get;
    private ServiceMethod[] post;
    private ServiceMethod[] put;
    private ServiceMethod[] delete;
    private ServiceMethod[] head;
    private ServiceMethod[] options;
    private ServiceMethod[] trace;
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(get, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(post, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(put, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(delete, request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(head, request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(options, request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(trace, request, response);
    }

    private void processRequest(ServiceMethod[] methods, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        try
        {
            String[] path = ServiceUtils.splitPath(request.getPathInfo());
            ServiceMethod method = findMatch(methods, path);
            method.invoke(this, request, response, path);
        }
        catch(MethodNotFoundException e)
        {
            String message = request.getPathInfo() + " didn't match any of the declared service methods!";
            log(message, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
        catch(IllegalArgumentException e)
        {
            String message = request.getPathInfo() + " contains invalid sections for the declared parameters!";
            log(message, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
        catch(IllegalAccessException e)
        {
            String message = request.getPathInfo() + " maps to a method that can't be accessed!";
            log(message, e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
        }
        catch(InvocationTargetException e)
        {
            String message = request.getPathInfo() + " maps to a method that could not be invoked!";
            log(message, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        }
    }
    
    public ServiceMethod findMatch(ServiceMethod[] methods, String[] path) throws MethodNotFoundException
    {
        for(ServiceMethod method : methods)
            if(method.matches(path))
                return method;

        throw new MethodNotFoundException();
    }

    private ServiceMethod[] loadMethods(Class<? extends Annotation> annotationClass)
    {
        List<ServiceMethod> methods = new ArrayList<ServiceMethod>();
        for(Method declaredMethod : getClass().getDeclaredMethods())
        {
            Annotation annotation = declaredMethod.getAnnotation(annotationClass);
            if(annotation != null)
            {
                try
                {
                    ServiceMethod method = new ServiceMethod(declaredMethod, annotation);
                    if(methods.contains(method))
                    {
                        ServiceMethod duplicate = methods.get(methods.indexOf(method));
                        log(String.format("Invalid service annotation! Duplicate annotation on \"%s(...)\" same as \"%s(...)\"!", declaredMethod.getName(), duplicate.getName()));
                    }
                    else 
                        methods.add(method);
                }
                catch(InvalidParameterException e)
                {
                    log(String.format("Invalid service annotation for '%s(...)' in class %s!", declaredMethod.getName(), getClass().getName()), e);
                }
            }
        }
        Collections.sort(methods, new ServiceMethodComparator());
        return methods.toArray(new ServiceMethod[methods.size()]);
    }
}