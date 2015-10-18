package ca.barelabs.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ca.barelabs.bareservice.RestServlet.OnValueReturnedCallback;

@Retention(RetentionPolicy.RUNTIME)
public @interface ValueReturnedCallback {
    Class<? extends OnValueReturnedCallback> value();
}
