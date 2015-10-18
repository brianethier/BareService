package ca.barelabs.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ca.barelabs.bareservice.RestServlet.OnServiceExceptionCallback;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExceptionCallback {
    Class<? extends OnServiceExceptionCallback> value();
}
