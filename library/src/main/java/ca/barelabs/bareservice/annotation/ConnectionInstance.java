package ca.barelabs.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ca.barelabs.bareservice.AbstractConnection;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectionInstance {
    Class<? extends AbstractConnection> value();
}
