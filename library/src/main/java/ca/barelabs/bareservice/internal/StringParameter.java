package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class StringParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        return value;
    }
}
