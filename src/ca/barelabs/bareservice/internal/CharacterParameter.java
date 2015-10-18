package ca.barelabs.bareservice.internal;


import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class CharacterParameter implements Parameter {
	
    @Override
    public Object toObject(String value) {
        if(value == null || value.length() != 1)
            throw new IllegalArgumentException("Failed to convert '" + value + "' to a Character!");
        
        return Character.valueOf(value.charAt(0));
    }
}
