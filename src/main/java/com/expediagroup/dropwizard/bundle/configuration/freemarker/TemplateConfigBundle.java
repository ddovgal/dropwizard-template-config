package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard {@link ConfiguredBundle} that wraps the currently configured
 * {@link io.dropwizard.configuration.ConfigurationSourceProvider} with a
 * {@link TemplateConfigurationSourceProvider}
 * that allows you to write your {@code config.yaml} as a
 * <a href="http://freemarker.org/">Freemarker</a> template.
 */
public class TemplateConfigBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private final TemplateConfigBundleConfiguration configuration;

    /**
     * Create a {@link TemplateConfigBundle} using the default configuration.
     */
    public TemplateConfigBundle() {
        this(new TemplateConfigBundleConfiguration());
    }

    /**
     * Create a {@link TemplateConfigBundle} using the given {@link TemplateConfigBundleConfiguration}.
     *
     * @param configuration The configuration for the new bundle. See {@link TemplateConfigBundleConfiguration}.
     */
    public TemplateConfigBundle(final TemplateConfigBundleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new TemplateConfigurationSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                configuration
        ));
    }

    @Override
    public void run(T configuration, Environment environment) {
        // intentionally left empty
    }

}
