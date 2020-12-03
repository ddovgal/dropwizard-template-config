package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The configuration for a {@link TemplateConfigBundle}
 */
public class TemplateConfigBundleConfiguration {

    private Charset charset = StandardCharsets.UTF_8;
    private String resourceIncludePath;
    private String fileIncludePath;
    private String outputPath;
    private Set<TemplateConfigVariablesProvider> customProviders = new LinkedHashSet<>();
    private Supplier<Object> factory = () -> customProviders().stream()
        .collect(Collectors.toMap(TemplateConfigVariablesProvider::getNamespace,
            Function.identity(),
            Providers::mergeProviders))
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getVariables()));

    /**
     * Initialize the {@link TemplateConfigBundle} with a custom set of {@link TemplateConfigVariablesProvider} instances.
     *
     * @param providers One or more {@link TemplateConfigurationSourceProvider} to use as custom providers
     */
    TemplateConfigBundleConfiguration(TemplateConfigVariablesProvider... providers) {
        if (Arrays.asList(providers).contains(null)) {
            throw new NullPointerException("List of TemplateConfigVariablesProviders contains null");
        }
        customProviders.addAll(Arrays.asList(providers));
    }

    /**
     * Initialize the {@link TemplateConfigBundle} with the default set of {@link TemplateConfigVariablesProvider} instances,
     * that is the provider that populates from System properties, and the provider that populates from Environment properties.
     */
    public TemplateConfigBundleConfiguration() {
        this(Providers.fromSystemProperties(), Providers.fromEnvironmentProperties());
    }

    /**
     * Factory to be used to create data model for freemarker engine to render template
     *
     * @return data model factory instance
     */
    public Supplier<Object> dataModelFactory() {
        return this.factory;
    }

    /**
     * Sets up model factory to be used by freemarker engine to render a template. Overrides default one.
     * Supplier should not return {@code null}, instead return empty.
     *
     * @param factory to set up
     * @return this configuration
     * @throws NullPointerException if provided {@code factory} is {@code null}.
     */
    public TemplateConfigBundleConfiguration dataModelFactory(Supplier<Object> factory) {
        if (factory == null) {
            throw new NullPointerException("Factory must not be null");
        }
        this.factory = factory;
        return this;
    }

    /**
     * Get the configured charset (Default: UTF-8)
     *
     * @return charset
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Set the {@link Charset} used to load, process, and output the config template
     * <p>The default is UTF-8.
     *
     * @param charset to set charset
     * @return this configuration
     */
    public TemplateConfigBundleConfiguration charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Get the configured resource include path (Default: None)
     *
     * @return Optional of resource include path
     */
    public Optional<String> resourceIncludePath() {
        return Optional.ofNullable(resourceIncludePath);
    }

    /**
     * Get the configured file include path (Default: None)
     *
     * @return Optional of file include path
     */
    public Optional<String> fileIncludePath() {
        return Optional.ofNullable(fileIncludePath);
    }

    /**
     * Get the configured output path for the processed config (Default: None)
     *
     * @return Optional of file output path
     */
    public Optional<String> outputPath() {
        return Optional.ofNullable(outputPath);
    }

    /**
     * Get the set of custom providers used to add variables to the configuration template (Default: Empty Set)
     *
     * @return set of this configuration
     */
    public Set<TemplateConfigVariablesProvider> customProviders() {
        return customProviders;
    }

    /**
     * Set the resource path from which config snippets will be included
     *
     * <p>Must not be {@code null}. By default there's no value set.
     * Only one of {@code resourceIncludePath} or {@code fileIncludePath}
     * may be specified.
     *
     * @param path the resource path
     * @return this configuration
     * @throws NullPointerException if provided {@code path} is {@code null}
     * @throws IllegalStateException if {@code fileIncludePath} is set
     */
    public TemplateConfigBundleConfiguration resourceIncludePath(String path) {
        if (path == null) {
            throw new NullPointerException("Provided resource path must not be null.");
        }
        if (fileIncludePath != null) {
            throw new IllegalStateException(
                "A value for fileIncludePath is already present; " +
                "only one of resourceIncludePath or fileIncludePath may be specified."
            );
        }
        this.resourceIncludePath = path;
        return this;
    }

    /**
     * Set the file path to include config snippets from
     *
     * <p>Must not be {@code null}. By default there's no value set.
     * Only one of {@code resourceIncludePath} or {@code fileIncludePath}
     * may be specified.
     *
     * @param path the file path
     * @return this configuration
     * @throws NullPointerException if provided {@code path} is {@code null}
     * @throws IllegalStateException if resourceIncludePath is already set
     */
    public TemplateConfigBundleConfiguration fileIncludePath(String path) {
        if (path == null) {
            throw new NullPointerException("Provided file path must not be null.");
        }
        if (resourceIncludePath != null) {
            throw new IllegalStateException(
                "A value for resourceIncludePath is already present; " +
                "only one of resourceIncludePath or fileIncludePath may be specified."
            );
        }
        this.fileIncludePath = path;
        return this;
    }

    /**
     * Set the path to output the filled-out config
     *
     * <p>Must not be {@code null}. By default there's no value set.
     *
     * @param outputPath to set the output
     * @return this configuration
     * @throws NullPointerException if provided {@code outputPath} is {@code null}
     */
    public TemplateConfigBundleConfiguration outputPath(String outputPath) {
        if (outputPath == null) {
            throw new NullPointerException("Provided output path must not be null.");
        }
        this.outputPath = outputPath;
        return this;
    }

    /**
     * Add a custom provider used to add your own variables to the configuration template.
     *
     * @param customProvider to add a custom provider
     * @return this configuration
     */
    public TemplateConfigBundleConfiguration addCustomProvider(TemplateConfigVariablesProvider customProvider) {
        this.customProviders.add(customProvider);
        return this;
    }
}
