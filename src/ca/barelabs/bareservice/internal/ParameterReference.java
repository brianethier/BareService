package ca.barelabs.bareservice.internal;

import ca.barelabs.bareservice.internal.ParameterFactory.Parameter;


public class ParameterReference {
	
    private final int index;
    private final String name;
    private final Parameter parameter;

    public ParameterReference(int index, String name, Parameter parameter) {
        this.index = index;
        this.name = name;
        this.parameter = parameter;
    }
    
    public int getIndex() {
        return index;
    }
    
    public String getName() {
        return name;
    }
    
    public Parameter getParameter() {
        return parameter;
    }
    
    public Object toObject(String[] path) {
        return parameter.toObject(path[index]);
    }   
}
