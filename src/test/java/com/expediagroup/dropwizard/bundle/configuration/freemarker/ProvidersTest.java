package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Various tests for Providers
 */
public class ProvidersTest {

    @Test
    public void fromMap() {
        TemplateConfigVariablesProvider provider = Providers.fromMap("fromMap", Collections.singletonMap("key", "value"));
        assertThat(provider).isNotNull();
        assertThat(provider.getNamespace()).isEqualTo("fromMap");
        assertThat(provider.getVariables()).containsEntry("key", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromMapWithEmptyNamespace() {
        TemplateConfigVariablesProvider provider = Providers.fromMap("", Collections.singletonMap("key", "value"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fromUnmodified() {
        TemplateConfigVariablesProvider provider =
            Providers.fromMap("fromMap", new HashMap<>(Collections.singletonMap("key", "value")));
        provider.getVariables().put("newKey", "newValue");
    }

    @Test
    public void fromSystemProperties() {
        TemplateConfigVariablesProvider provider = Providers.fromSystemProperties();
        TemplateConfigVariablesProvider systemProperties = Providers.fromProperties("sys", System.getProperties());
        assertThat(provider.getNamespace()).isEqualTo(systemProperties.getNamespace());
        assertThat(provider.getVariables()).contains(systemProperties.getVariables().entrySet().toArray(new Entry[0]));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fromSystemPropertiesUnmodified() {
        TemplateConfigVariablesProvider provider = Providers.fromSystemProperties();
        provider.getVariables().put("newKey", "newValue");
    }

    @Test
    public void fromEnvironmentProperties() {
        TemplateConfigVariablesProvider provider = Providers.fromEnvironmentProperties();
        TemplateConfigVariablesProvider environmentProperties = Providers.fromMap("env", System.getenv());
        assertThat(provider.getNamespace()).isEqualTo(environmentProperties.getNamespace());
        assertThat(provider.getVariables()).contains(environmentProperties.getVariables().entrySet().toArray(new Entry[0]));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fromEnvironmentPropertiesUnmodified() {
        TemplateConfigVariablesProvider provider = Providers.fromEnvironmentProperties();
        provider.getVariables().put("newKey", "newValue");
    }

    @Test
    public void fromProperties() {
        Properties properties = new Properties();
        properties.put("key", "value");
        TemplateConfigVariablesProvider provider = Providers.fromProperties("fromProperties", properties);
        assertThat(provider.getNamespace()).isEqualTo("fromProperties");
        assertThat(provider.getVariables()).containsEntry("key", "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fromPropertiesUnmodified() {
        TemplateConfigVariablesProvider provider = Providers.fromProperties("fromProperties", new Properties());
        provider.getVariables().put("newKey", "newValue");
    }

    @Test
    public void fromPropertiesURL() throws IOException {
        TemplateConfigVariablesProvider provider =
            Providers.fromProperties("fromPropertiesURL", getClass().getResource("/config-snippets/test.properties"));
        assertThat(provider.getNamespace()).isEqualTo("fromPropertiesURL");
        assertThat(provider.getVariables()).containsEntry("propertyKey", "propertyValue");
    }

    @Test(expected = NullPointerException.class)
    public void fromPropertiesURLNotFound() throws IOException {
        Providers.fromProperties("fromPropertiesURL", getClass().getResource("/config-snippets/notfound.properties"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void fromPropertiesURLUnmodified() throws IOException {
        TemplateConfigVariablesProvider provider =
            Providers.fromProperties("fromPropertiesURL", getClass().getResource("/config-snippets/test.properties"));
        provider.getVariables().put("newKey", "newValue");
    }

    @Test
    public void mergeProvidersSameNamespace() {
        TemplateConfigVariablesProvider leftProvider =
            Providers.fromMap("mergeProviders", new HashMap<>(Collections.singletonMap("key", "value1")));
        TemplateConfigVariablesProvider provider2 =
            Providers.fromMap("mergeProviders", new HashMap<>(Collections.singletonMap("key", "value2")));
        TemplateConfigVariablesProvider mergedProviders = Providers.mergeProviders(leftProvider, provider2);

        assertThat(mergedProviders.getNamespace()).isEqualTo("mergeProviders");
        assertThat(mergedProviders.getVariables()).containsEntry("key", "value2");
    }

    @Test
    public void mergeProvidersDifferentNamespace() {
        TemplateConfigVariablesProvider leftProvider =
            Providers.fromMap("mergeProviders1", new HashMap<>(Collections.singletonMap("key", "value1")));
        TemplateConfigVariablesProvider rightProvider =
            Providers.fromMap("mergeProviders2", new HashMap<>(Collections.singletonMap("key", "value2")));
        TemplateConfigVariablesProvider mergedProviders = Providers.mergeProviders(leftProvider, rightProvider);

        assertThat(mergedProviders.getNamespace()).isEqualTo("mergeProviders2");
        assertThat(mergedProviders.getVariables()).containsEntry("key", "value2");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void mergeProvidersUnmodified() {
        TemplateConfigVariablesProvider leftProvider =
            Providers.fromMap("mergeProviders", new HashMap<>(Collections.singletonMap("key", "value1")));
        TemplateConfigVariablesProvider rightProvider =
            Providers.fromMap("mergeProviders", new HashMap<>(Collections.singletonMap("key", "value2")));
        TemplateConfigVariablesProvider mergedProviders = Providers.mergeProviders(leftProvider, rightProvider);

        mergedProviders.getVariables().put("newKey", "newValue");
    }

}
