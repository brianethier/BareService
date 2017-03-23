/*
 * Copyright 2015 Brian Ethier
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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.barelabs.bareservice.internal.GZipServletResponseWrapper;


public abstract class AbstractConnection {

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";    
    public static final String ENCODING_GZIP = "gzip";

    private HttpServletRequest mRequest;
    private HttpServletResponse mResponse;


    void init(HttpServletRequest request, HttpServletResponse response) {
    	String acceptEncoding = request.getHeader(HEADER_ACCEPT_ENCODING);
    	if (acceptEncoding != null && acceptEncoding.indexOf(ENCODING_GZIP) != -1) {
    		response.setHeader(HEADER_CONTENT_ENCODING, ENCODING_GZIP);
            mRequest = request;
            mResponse = new GZipServletResponseWrapper(response);
    	} else {
            mRequest = request;
            mResponse = response;
    	}
    }

    void close() throws IOException {
		onClose();
        if (mResponse instanceof Closeable) {
        	((Closeable) mResponse).close();
        }
    }

    public HttpServletRequest getRequest() {
        return mRequest;
    }

    public HttpServletResponse getResponse() {
        return mResponse;
    }
    
    public List<String> getStringParamValues(String key) {
    	String[] values = mRequest.getParameterValues(key);
        return values == null ? new ArrayList<String>() : Arrays.asList(values);
    }
    
    public String getStringParam(String key, String defValue) {
        String value = mRequest.getParameter(key);
        return value == null ? defValue : value;
    }
    
    public boolean getBooleanParam(String key, boolean defValue) {
        String value = mRequest.getParameter(key);
        return value == null ? defValue : Boolean.parseBoolean(value);
    }
    
    public float getFloatParam(String key, float defValue) {
        try {
            String value = mRequest.getParameter(key);
            return value == null ? defValue : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }
    
    public int getIntParam(String key, int defValue) {
        try {
            String value = mRequest.getParameter(key);
            return value == null ? defValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }
    
    public long getLongParam(String key, long defValue) {
        try {
            String value = mRequest.getParameter(key);
            return value == null ? defValue : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }
    
    public abstract void onConnected(boolean isGuest) throws ServletException, IOException;
    
    public abstract void onValueReturned(Object value) throws IOException;
    
    public abstract void onClose() throws IOException;
    
    public abstract boolean onServiceException(Throwable throwable) throws IOException;
}
