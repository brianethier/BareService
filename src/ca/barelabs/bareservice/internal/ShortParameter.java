package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class ShortParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Short.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Short!", e);
        }
    }
}
