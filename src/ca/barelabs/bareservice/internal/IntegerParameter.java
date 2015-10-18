package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class IntegerParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        try {
            return Integer.valueOf(value);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Integer!", e);
        }
    }
}
