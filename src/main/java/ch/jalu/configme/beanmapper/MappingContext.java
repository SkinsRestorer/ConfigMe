package ch.jalu.configme.beanmapper;

import ch.jalu.configme.utils.TypeInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds necessary information for a certain value that is being mapped in the bean mapper.
 */
public interface MappingContext {

    /**
     * Creates a child context with the given path addition ("name") and type information.
     *
     * @param name additional path element to append to this context's path
     * @param typeInformation the required type
     * @return new child context
     */
    @NotNull MappingContext createChild(@NotNull String name, @NotNull TypeInformation typeInformation);

    /**
     * @return the required type the value needs to be mapped to
     */
    @NotNull TypeInformation getTypeInformation();

    /**
     * Convenience method: gets the generic type info for the given index and ensures that the generic type information
     * exists and that it can be converted into a safe-to-write class. Throws an exception otherwise.
     *
     * @param index the index to get generic type info for
     * @return the generic type info (throws exception if absent or not precise enough)
     */
    default @Nullable TypeInformation getGenericTypeInfoOrFail(int index) {
        TypeInformation genericType = getTypeInformation().getGenericType(index);
        if (genericType == null || genericType.getSafeToWriteClass() == null) {
            throw new ConfigMeMapperException(this,
                "The generic type " + index + " is not well defined");
        }
        return getTypeInformation().getGenericType(index);
    }

    /**
     * @return textual representation of the info in the context, used in exceptions
     */
    @NotNull String createDescription();

    /**
     * Registers an error during the mapping process, which delegates to the supplied
     * {@link ch.jalu.configme.properties.convertresult.ConvertErrorRecorder ConvertErrorRecorder},
     * associated to the property this conversion is being performed for.
     *
     * @param reason the error reason (ignored by the default context implementation)
     */
    void registerError(@NotNull String reason);

}
