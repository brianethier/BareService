package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class BooleanParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        if(value != null && value.length() == 1 && value.equals("1"))
            return true;

        return Boolean.valueOf(value);
    }
}
