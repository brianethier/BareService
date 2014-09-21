package com.barenode.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.barenode.bareservice.RestServlet.OnValueReturnedCallback;

@Retention(RetentionPolicy.RUNTIME)
public @interface ValueReturnedCallback {
    Class<? extends OnValueReturnedCallback> value();
}
