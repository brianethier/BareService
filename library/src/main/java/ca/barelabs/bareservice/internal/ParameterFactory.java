package ca.barelabs.bareservice.internal;


public final class ParameterFactory {
    
    public interface Parameter {
        Object toObject(String value);
    }

    
    public static Parameter create(Class<?> type) {
        if(type.isAssignableFrom(Byte.TYPE) || type.isAssignableFrom(Byte.class))
            return new ByteParameter();
        if(type.isAssignableFrom(Short.TYPE) || type.isAssignableFrom(Short.class))
            return new ShortParameter();
        if(type.isAssignableFrom(Integer.TYPE) || type.isAssignableFrom(Integer.class))
            return new IntegerParameter();
        if(type.isAssignableFrom(Long.TYPE) || type.isAssignableFrom(Long.class))
            return new LongParameter();
        if(type.isAssignableFrom(Float.TYPE) || type.isAssignableFrom(Float.class))
            return new FloatParameter();
        if(type.isAssignableFrom(Double.TYPE) || type.isAssignableFrom(Double.class))
            return new DoubleParameter();
        if(type.isAssignableFrom(Character.TYPE) || type.isAssignableFrom(Character.class))
            return new CharacterParameter();
        if(type.isAssignableFrom(Boolean.TYPE) || type.isAssignableFrom(Boolean.class))
            return new BooleanParameter();
        if(type.isAssignableFrom(String.class))
            return new StringParameter();

        throw new IllegalArgumentException("Invalid parameter type: " + type.getName());
    }
}
