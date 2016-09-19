package com.github.authme.configme.beanmapper;

import com.github.authme.configme.resource.PropertyResource;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.authme.configme.beanmapper.MapperUtils.getBeanProperty;
import static com.github.authme.configme.beanmapper.MapperUtils.getGenericClassesSafely;
import static com.github.authme.configme.beanmapper.MapperUtils.getWritableProperties;
import static com.github.authme.configme.beanmapper.MapperUtils.invokeDefaultConstructor;
import static com.github.authme.configme.beanmapper.MapperUtils.setBeanProperty;

/**
 * Maps a section of a property resource to the provided JavaBean class. The mapping is based on the field names,
 * which must correspond to the field in the property resource. For example, if a JavaBean class has a field called
 * {@code length} and should be mapped from the property resource's value at path {@code definition}, the mapper will
 * look up {@code definition.length} to get the value for the JavaBean field.
 * <p>
 * Classes must be JavaBeans. These are simple classes with private fields, accompanied with getters and setters.
 * <b>The mapper only considers fields which have an associated setter method.</b> JavaBean classes without any setter
 * method are considered as type that needs to be converted to. To support these, you need to implement your own
 * {@link Transformer} and instantiate the mapper with it.
 * <p>
 * <b>Recursion:</b> the mapping of values to a JavaBean is performed recursively, i.e. a JavaBean may have other
 * JavaBeans as fields at an arbitrary "depth."
 * <p>
 * <b>Collections</b> are only supported if they are explicitly typed, i.e. a field of {@code List&lt;String>}
 * is supported but {@code List&lt;?>} or {@code List&lt;T extends Number>} are not supported. Specifically, you may
 * only declare fields of type {@link List} or {@link Set}, or a parent type ({@link Collection} or {@link Iterable}).
 * Fields of type <b>Map</b> are supported also.
 * <p>
 * JavaBeans may have <b>optional fields</b>. If the mapper cannot map the property resource value to the corresponding
 * field, it only treats it as a failure if the field's value is {@code null}. If it has a default
 * value assigned to it, the default value remains and the mapping process continues. A JavaBean field whose value is
 * {@code null} signifies a failure and stops the mapping process immediately.
 */
public class Mapper {

    private final Transformer[] transformers;

    /**
     * Creates a new JavaBean mapper with the default type transformers.
     */
    public Mapper() {
        this(Transformers.getDefaultTransformers());
    }

    /**
     * Creates a new JavaBean mapper with the given transformers.
     *
     * @param transformers the transformers to use for mapping values
     * @see Transformers#getDefaultTransformers
     */
    public Mapper(Transformer... transformers) {
        this.transformers = transformers;
    }

    @Nullable
    public <T> Map<String, T> createMap(String path, PropertyResource resource, Class<T> clazz) {
        Object value = resource.getObject(path);
        return (Map<String, T>) processMap(Map.class, new GenericMapType(String.class, clazz), value);
    }

    /**
     * Converts the value in the property resource at the given path to the provided beans class.
     *
     * @param path the path to convert from
     * @param resource the property resource to read from
     * @param clazz the JavaBean class
     * @param <T> the bean type
     * @return the converted bean, or null if not possible
     */
    @Nullable
    public <T> T convertToBean(String path, PropertyResource resource, Class<T> clazz) {
        return (T) getPropertyValue(clazz, null, resource.getObject(path));
    }

    /**
     * Returns a value of type {@code clazz} based on the provided {@code value} if possible.
     *
     * @param clazz the desired type to return
     * @param genericType the generic type if applicable
     * @param value the value to convert from
     * @return the converted value, or {@code null} if not possible
     */
    @Nullable
    protected Object getPropertyValue(Class<?> clazz, @Nullable Type genericType, @Nullable Object value) {
        Object result;
        if ((result = processCollection(clazz, genericType, value)) != null) {
            return result;
        } else if ((result = processMap(clazz, genericType, value)) != null) {
            return result;
        } else if ((result = processTransformers(clazz, genericType, value)) != null) {
            return result;
        }
        return convertToBean(clazz, value);
    }

    // Handles List and Set fields
    @Nullable
    protected Collection<?> processCollection(Class<?> clazz, Type genericType, Object value) {
        if (Iterable.class.isAssignableFrom(clazz) && value instanceof Iterable<?>) {
            Class<?> collectionType = MapperUtils.getGenericClassSafely(genericType);
            List list = new ArrayList<>();
            for (Object o : (Iterable<?>) value) {
                list.add(getPropertyValue(collectionType, null, o));
            }

            if (clazz.isAssignableFrom(List.class)) {
                return list;
            } else if (clazz.isAssignableFrom(Set.class)) {
                return new HashSet<>(list);
            } else {
                throw new ConfigMeMapperException("Unsupported collection type '" + clazz + "' encountered");
            }
        }
        return null;
    }

    // Handles Map fields
    @Nullable
    protected Map<?, ?> processMap(Class<?> clazz, Type genericType, Object value) {
        if (Map.class.isAssignableFrom(clazz) && value instanceof Map<?, ?>) {
            Map<String, ?> entries = (Map<String, ?>) value;
            Class<?>[] mapTypes = getGenericClassesSafely(genericType);
            if (mapTypes[0] != String.class) {
                // TODO: For now the key can only be a String
                throw new ConfigMeMapperException("Map key type can only be String for now");
            }
            Map result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : entries.entrySet()) {
                result.put(entry.getKey(), getPropertyValue(mapTypes[1], null, entry.getValue()));
            }
            return result;
        }
        return null;
    }

    // Passes value to Transformers
    @Nullable
    protected Object processTransformers(Class<?> type, Type genericType, Object value) {
        Object result;
        for (Transformer transformer : transformers) {
            result = transformer.transform(type, genericType, value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Converts the provided value to the requested JavaBeans class if possible.
     *
     * @param <T> the JavaBean type
     * @param clazz the JavaBean class
     * @param value the value from the property resource
     * @return the converted value, or null if not possible
     */
    @Nullable
    protected <T> T convertToBean(Class<T> clazz, Object value) {
        List<PropertyDescriptor> properties = getWritableProperties(clazz);
        // Check that we have properties (or else we don't have a bean) and that the provided value is a Map
        // so we can execute the mapping process.
        if (properties.isEmpty() || !(value instanceof Map<?, ?>)) {
            return null;
        }

        Map<?, ?> entries = (Map<?, ?>) value;
        T bean = invokeDefaultConstructor(clazz);
        for (PropertyDescriptor propertyDescriptor : properties) {
            Object result = getPropertyValue(
                propertyDescriptor.getPropertyType(),
                propertyDescriptor.getWriteMethod().getGenericParameterTypes()[0],
                entries.get(propertyDescriptor.getName()));
            if (result != null) {
                setBeanProperty(propertyDescriptor, bean, result);
            } else if (getBeanProperty(propertyDescriptor, bean) == null) {
                // TODO: Allow to set exception mode
                throw new ConfigMeMapperException("No suitable value found for mandatory property '"
                    + propertyDescriptor.getName() + "' in '" + clazz + "'");
            }
        }
        return bean;
    }

    private static final class GenericMapType implements ParameterizedType {

        private final Class<?>[] actualTypeArguments;

        GenericMapType(Class<?> keyType, Class<?> valueType) {
            this.actualTypeArguments = new Class<?>[]{keyType, valueType};
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
