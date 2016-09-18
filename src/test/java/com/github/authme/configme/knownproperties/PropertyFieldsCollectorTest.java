package com.github.authme.configme.knownproperties;

import com.github.authme.configme.TestUtils;
import com.github.authme.configme.properties.Property;
import com.github.authme.configme.samples.TestConfiguration;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link PropertyFieldsCollector}.
 */
public class PropertyFieldsCollectorTest {

    @Test
    public void shouldGetAllProperties() {
        // given / when
        List<PropertyEntry> knownProperties = PropertyFieldsCollector.getAllProperties(
            TestConfiguration.class, AdditionalTestConfiguration.class);

        // then
        // 3 properties in AdditionalTestConfiguration, 10 properties in TestConfiguration
        assertThat(knownProperties, hasSize(13));
        // Take some samples, check for presence & expected comments
        assertHasPropertyWithComments(knownProperties, TestConfiguration.SKIP_BORING_FEATURES, "Skip boring features?");
        assertHasPropertyWithComments(knownProperties, TestConfiguration.DUST_LEVEL);
        assertHasPropertyWithComments(knownProperties, TestConfiguration.VERSION_NUMBER,
            "The version number", "This is just a random number");
        assertHasPropertyWithComments(knownProperties, AdditionalTestConfiguration.NAME, "Additional name");
        assertHasPropertyWithComments(knownProperties, AdditionalTestConfiguration.SLEEP, "Seconds to sleep");
    }

    @Test
    public void shouldHavePrivateConstructor() {
        TestUtils.validateHasOnlyPrivateEmptyConstructor(PropertyFieldsCollector.class);
    }

    private static void assertHasPropertyWithComments(List<PropertyEntry> knownProperties, Property<?> property,
                                                      String... comments) {
        for (PropertyEntry entry : knownProperties) {
            if (entry.getProperty().equals(property)) {
                assertThat(entry.getComments(), equalTo(comments));
                return;
            }
        }
        fail("Not found in property map: " + property);
    }

}