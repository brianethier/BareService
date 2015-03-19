package com.barenode.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.barenode.bareservice.RestServlet.OnServiceExceptionCallback;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExceptionCallback {
    Class<? extends OnServiceExceptionCallback> value();
}
