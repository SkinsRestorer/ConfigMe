package ch.jalu.configme.properties.inlinearray;

import org.jetbrains.annotations.NotNull;

/**
 * Converter from a simple String value to an array of multiple values.
 * Used in {@link ch.jalu.configme.properties.InlineArrayProperty}.
 *
 * @param <T> the array type
 */
public interface InlineArrayConverter<T> {

    /**
     * Converts from the String to an array of the converter's type.
     *
     * @param in the string to convert from
     * @return array with elements based on the input String, never null
     */
    @NotNull T @NotNull [] fromString(String in);

    /**
     * Converts the provided array to its String representation (opposite of {@link #fromString(String)}).
     *
     * @param value the value to convert
     * @return String representation of the array
     */
    @NotNull String toExportValue(@NotNull T @NotNull [] value);

}
