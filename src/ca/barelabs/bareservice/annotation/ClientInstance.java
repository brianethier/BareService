package ca.barelabs.bareservice.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ca.barelabs.bareservice.RestClient;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClientInstance {
    Class<? extends RestClient> value();
}
