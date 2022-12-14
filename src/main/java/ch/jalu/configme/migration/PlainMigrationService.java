package ch.jalu.configme.migration;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.convertresult.PropertyValue;
import ch.jalu.configme.resource.PropertyReader;
import org.jetbrains.annotations.NotNull;

/**
 * Simple migration service that can be extended.
 */
public class PlainMigrationService implements MigrationService {

    @Override
    public boolean checkAndMigrate(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        if (performMigrations(reader, configurationData) == MIGRATION_REQUIRED
            || !configurationData.areAllValuesValidInResource()) {
            return MIGRATION_REQUIRED;
        }
        return NO_MIGRATION_NEEDED;
    }

    /**
     * Override this method for custom migrations. This method is executed before checking
     * if all settings are present. For instance, you could implement deleting obsolete properties
     * and rename properties in this method.
     * <p>
     * Note that the settings manager automatically saves the resource
     * if the migration service returns {@link #MIGRATION_REQUIRED} from {@link #checkAndMigrate}.
     *
     * @param reader the reader with which the configuration file can be read
     * @param configurationData the configuration data
     * @return true if a migration has been performed, false otherwise (see constants on {@link MigrationService})
     */
    protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        return NO_MIGRATION_NEEDED;
    }

    /**
     * Utility method: moves the value of an old property to a new property. This is only done if there is no value for
     * the new property in the configuration file and if there is one for the old property. Returns true if a value is
     * present at the old property path.
     *
     * @param oldProperty the old property (create a temporary {@link Property} object with the path)
     * @param newProperty the new property to move the value to
     * @param reader the property reader to read the configuration file from
     * @param configurationData configuration data to update a property's value
     * @param <T> the type of the property
     * @return true if the old path exists in the configuration file, false otherwise
     */
    protected static <T> boolean moveProperty(@NotNull Property<T> oldProperty, @NotNull Property<T> newProperty,
                                              @NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
        if (reader.contains(oldProperty.getPath())) {
            if (!reader.contains(newProperty.getPath())) {
                PropertyValue<T> value = oldProperty.determineValue(reader);
                configurationData.setValue(newProperty, value.getValue());
            }
            return true;
        }
        return false;
    }
}
