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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class AbstractConnection {

    private HttpServletRequest mRequest;
    private HttpServletResponse mResponse;    

    public final void init(HttpServletRequest request, HttpServletResponse response) {
        mRequest = request;
        mResponse = response;
    }

    public HttpServletRequest getRequest() {
        return mRequest;
    }

    public HttpServletResponse getResponse() {
        return mResponse;
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
    
    public abstract void onConnected(boolean isGuest) throws Exception;
    
    public abstract void onValueReturned(Object value) throws Exception;
    
    public abstract boolean onServiceException(Throwable throwable) throws ServletException, IOException;

}
