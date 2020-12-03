package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.dropwizard.configuration.ConfigurationSourceProvider;

/**
 * An implementation of Dropwizard {@link ConfigurationSourceProvider} that extends an existing {@link ConfigurationSourceProvider} instance
 * with <a href="http://freemarker.org/">Freemarker</a> functionality.
 */
public class TemplateConfigurationSourceProvider implements ConfigurationSourceProvider {

    private final ConfigurationSourceProvider parentProvider;
    private final TemplateConfigBundleConfiguration configuration;

    TemplateConfigurationSourceProvider(
            final ConfigurationSourceProvider parentProvider,
            final TemplateConfigBundleConfiguration configuration
    ) {
        this.parentProvider = parentProvider;
        this.configuration = configuration;
    }

    @Override
    public InputStream open(final String path) throws IOException {
        try {
            return createConfigurationSourceStream(path);
        } catch (TemplateException e) {
            throw new IllegalStateException("Could not render template.", e);
        }
    }

    private InputStream createConfigurationSourceStream(String path) throws IOException, TemplateException {
        Configuration freemarkerConfiguration = createFreemarkerConfiguration();
        Template configTemplate = createFreemarkerTemplate(path, freemarkerConfiguration);
        byte[] processedConfigTemplate = processTemplate(Objects.requireNonNull(configuration.dataModelFactory().get()), configTemplate);
        writeConfigFile(processedConfigTemplate);
        return new ByteArrayInputStream(processedConfigTemplate);
    }

    private Configuration createFreemarkerConfiguration() {
        Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_22);
        freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfiguration.setNumberFormat("computer");
        freemarkerConfiguration.setDefaultEncoding(configuration.charset().name());
        configuration.resourceIncludePath().ifPresent(p -> freemarkerConfiguration.setClassForTemplateLoading(
            TemplateConfigurationSourceProvider.class, !p.startsWith("/") ? ("/" + p) : p));

        if (!configuration.resourceIncludePath().isPresent()) {
            configuration.fileIncludePath().map(File::new).ifPresent(f -> {
                try {
                    freemarkerConfiguration.setDirectoryForTemplateLoading(f);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not set directory for template loading.", e);
                }
            });
        }
        return freemarkerConfiguration;
    }

    private Template createFreemarkerTemplate(String path, Configuration freemarkerConfiguration) throws IOException {
        InputStream configurationSource = parentProvider.open(path);
        InputStreamReader configurationSourceReader = new InputStreamReader(configurationSource, configuration.charset());
        return new Template("config", configurationSourceReader, freemarkerConfiguration);
    }

    private byte[] processTemplate(Object dataModel, Template template) throws TemplateException, IOException {
        ByteArrayOutputStream processedTemplateStream = new ByteArrayOutputStream();
        template.process(dataModel, new OutputStreamWriter(processedTemplateStream, configuration.charset()));
        return processedTemplateStream.toByteArray();
    }

    private void writeConfigFile(byte[] processedTemplateBytes) {
        configuration.outputPath().ifPresent(pathString -> {
            try {
                Path path = Paths.get(pathString).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.write(path,
                            processedTemplateBytes,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new IllegalStateException("Could not write configuration file.", e);
            }
        });
    }
}
