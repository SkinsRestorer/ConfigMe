package ch.jalu.configme.properties;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * List property of a configurable type. The lists are immutable.
 *
 * @param <T> the property type
 */
public class ListProperty<T> extends BaseProperty<List<T>> {

    private final PropertyType<T> type;

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param type the property type
     * @param defaultValue the entries in the list of the default value
     */
    @SafeVarargs
    public ListProperty(@NotNull String path, @NotNull PropertyType<T> type, @NotNull T @NotNull ... defaultValue) {
        this(path, type, Arrays.asList(defaultValue));
    }

    /**
     * Constructor.
     *
     * @param path the path of the property
     * @param type the property type
     * @param defaultValue the default value of the property
     */
    public ListProperty(@NotNull String path, @NotNull PropertyType<T> type, @NotNull List<T> defaultValue) {
        super(path, Collections.unmodifiableList(defaultValue));
        Objects.requireNonNull(type, "type");
        this.type = type;
    }

    @Override
    protected @Nullable List<T> getFromReader(@NotNull PropertyReader reader, @NotNull ConvertErrorRecorder errorRecorder) {
        List<?> list = reader.getList(getPath());

        if (list != null) {
            return Collections.unmodifiableList(list.stream()
                .map(elem -> type.convert(elem, errorRecorder))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        }
        return null;
    }

    @Override
    public @NotNull Object toExportValue(@NotNull List<T> value) {
        return value.stream()
            .map(type::toExportValue)
            .collect(Collectors.toList());
    }
}
