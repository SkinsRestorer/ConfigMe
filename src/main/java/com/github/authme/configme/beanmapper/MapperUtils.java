package com.github.authme.configme.beanmapper;

import com.github.authme.configme.exception.ConfigMeException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper utilities.
 */
final class MapperUtils {

    private MapperUtils() {
    }

    /**
     * Gets the generic type of the property type. Throws an exception if it is not a concrete class.
     *
     * @param type the type declaration
     * @return the generic type
     * @throws ConfigMeMapperException if there is no generic type or it is not a concrete class
     */
    static Class<?> getGenericClassSafely(Type type) {
        Type genericType = getGenericTypes(type, 1)[0];
        if (genericType instanceof Class<?>) {
            return (Class<?>) genericType;
        }
        throw new ConfigMeMapperException("Type '" + genericType + "' does not have a concrete generic type,"
            + " found '" + genericType + "'");
    }

    /**
     * Returns the first two generic types of a class. Throws an exception if any generic type is not a concrete
     * class or if the base class does not have at least two generic types.
     *
     * @param type the type declaration
     * @return the (first) two generic types
     * @throws ConfigMeMapperException if there aren't two generic types or if one is not a concrete class
     */
    static Class<?>[] getGenericClassesSafely(Type type) {
        Type[] types = getGenericTypes(type, 2);

        if (!(types[0] instanceof Class<?>)) {
            throw new ConfigMeMapperException("Type '" + types[0] + "' does not have a concrete generic type,"
                + " found '" + types[0] + "'");
        } else if (!(types[1] instanceof Class<?>)) {
            throw new ConfigMeMapperException("Type '" + types[1] + "' does not have a concrete generic type,"
                + " found '" + types[1] + "'");
        }

        return new Class<?>[]{ (Class<?>) types[0], (Class<?>) types[1] };
    }

    private static Type[] getGenericTypes(Type type, int numberOfGenericTypes) {
        // Type type = descriptor.getWriteMethod().getGenericParameterTypes()[0];
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] genericTypes = pt.getActualTypeArguments();
            if (genericTypes.length >= numberOfGenericTypes) {
                return genericTypes;
            } else {
                throw new ConfigMeMapperException("Type '" + type + "' has less than "
                    + numberOfGenericTypes + " generic types");
            }
        }
        throw new ConfigMeMapperException("Type '" + type + "' is not a parameterized type");
    }

    static List<PropertyDescriptor> getWritableProperties(Class<?> clazz) {
        PropertyDescriptor[] descriptors;
        try {
            descriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }
        List<PropertyDescriptor> writableProperties = new ArrayList<>(descriptors.length);
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getWriteMethod() != null) {
                writableProperties.add(descriptor);
            }
        }
        return writableProperties;
    }

    static void setBeanProperty(PropertyDescriptor property, Object bean, Object value) {
        try {
            property.getWriteMethod().invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    static Object getBeanProperty(PropertyDescriptor property, Object bean) {
        if (property.getReadMethod() == null) {
            return null;
        }
        try {
            return property.getReadMethod().invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    static <T> T invokeDefaultConstructor(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigMeException("Could not create object of type '" + clazz.getName()
                + "'. It is required to have a default constructor.", e);
        }
    }

}
