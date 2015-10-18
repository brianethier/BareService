package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class LongParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Long.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Long!", e);
        }
    }
}
