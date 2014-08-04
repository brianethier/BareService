# BareService - simple/lightweight HTTP service

BareService is a lightweight HttpServlet wrapper that maps HttpServlet requests to individual methods based on the requested path. The user must handle the requests/responses like a regular doGet(...), doPost(...)... in a normal HttpServlet but now the user could define an API interface with multiple related paths in one Service instance instead of each in a separate HttpServlet.  

I'm sure there are ample reasons to use some the frameworks out there like Spring, Jersey... for HTTP services but I wanted to a) have something as easy as a servlet to set up and get running and since I only needed to handle data in JSON, I really only needed the framework to handle mapping paths to methods to use for creating dummy services for app development b) better understand / learn Java annotation, reflection...

## Overview

To create a service you simple extend HttpService which is derived from HttpServlet. Since this is an HttpServlet you need to annotate it or declare the path mapping in the web.xml like you would for a regular HttpServlet. Only difference is, now you should end the path with a wildcard to defer handling more specific path names to individual service methods.

So you would annotate the HttpService class with *@WebServlet("/service/\*")* if you wanted to have a service to handle:
```
GET /service
GET /service/path
POST /service/path
GET /service/another/path
...
```
And now you simple define the methods for those paths by annotating them with one of the following annotations (from com.barenode.bareservice.annotation.\*) depending on what HTTP method you want to handle: GET, POST, PUT, DELETE, HEAD, TRACE or OPTIONS. Each should include the path it wants to define starting from the wildcard section on. i.e. "/another/path" for "/service/another/path" in the example above.

## Path Parameters

You can also define parameters in the path that will be converted to Java primitives (or primitive wrapper types i.e. Integer, Double...) and passed as arguments to the method. These parameters are declared with braces i.e {name} and replace path sections. The number of path parameters must match the number or declared method parameters but they can replace any path section as in /{name1}/send/{name2}

So if you declare GET("/service/{id}") and add *int id* to the method signature, then calls to /service/1234 or /service/<anything> would attempt to convert that value to an integer and pass it to the method.

## Path Resolution

Path resolution with try to find the best match starting from the longest declared path. If there are two methods that have the same number of sections and the names match but one is because of a declared parameter then the one with a direct match would map first. So calling '/service/path' would map to '/service/path' and not '/service/{name}' regardless of the order that they were declared.

Also if you declare two paths with swapped parameters and section names, that is '/service/{name}/get' and '/service/name/{get}', then calling '/service/name/get' would map from the section names first. So in this case, '/service/name/{get}' would be called using 'get' as the argument.

## Example
```
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.barenode.bareservice.RestServlet;
import com.barenode.bareservice.annotation.GET;

// Regular WebServlet annotation
@WebServlet("/service/*")
public class ItemService extends RestServlet
{
    @GET
    public void get(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        // Calls to '/service' handled here.
    }

    @GET("/items")
    public void getItems(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        // Calls to '/service/items' handled here.
    }

    @GET("/items/{id}")
    public void getItems(HttpServletRequest request, HttpServletResponse response, String id) throws ServletException
    {
        // Calls to '/service/items/<any text>' handled here. See *Path Parameters*.
    }
}
```


*Note: HttpService internally uses reflection for method calls but all methods are parsed and cached when the Servlet first loads minimizing the reflection look-up cost on each request so request won't be quite as fast as a compiled method call but close. Also the method names do not matter, only the annotated paths do.*
