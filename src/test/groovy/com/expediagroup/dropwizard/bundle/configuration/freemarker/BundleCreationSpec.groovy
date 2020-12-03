package com.expediagroup.dropwizard.bundle.configuration.freemarker

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class BundleCreationSpec extends Specification {

    def 'using the default constructor creates bundle with default config'() {
        when:
        def bundle = new TemplateConfigBundle()

        then:
        bundle.configuration.charset == StandardCharsets.UTF_8
        bundle.configuration.resourceIncludePath == null
        bundle.configuration.fileIncludePath == null
        bundle.configuration.customProviders.size() == 2
    }

    def 'a specific configuration with null provider throws exception'() {
        when:
        new TemplateConfigBundle(
            new TemplateConfigBundleConfiguration(null)
                .charset(StandardCharsets.US_ASCII)
                .resourceIncludePath('includePath')
        )

        then:
        thrown NullPointerException
    }

    def 'a specific configuration with null provider in providers list throws exception'() {
        when:
        new TemplateConfigBundle(
            new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), null)
                .charset(StandardCharsets.US_ASCII)
                .resourceIncludePath('includePath')
        )

        then:
        thrown NullPointerException
    }

    def 'a specific configuration can be applied'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .charset(StandardCharsets.US_ASCII)
                        .resourceIncludePath('includePath')
        )

        then:
        bundle.configuration.charset == StandardCharsets.US_ASCII
        bundle.configuration.resourceIncludePath == 'includePath'
    }

    def 'a specific configuration with a null resourceIncludePath throws exception'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .charset(StandardCharsets.US_ASCII)
                        .resourceIncludePath(null)
        )

        then:
        thrown NullPointerException
    }

        def 'a specific configuration with a null fileIncludePath throws exception'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .charset(StandardCharsets.US_ASCII)
                        .fileIncludePath(null)
        )

        then:
        thrown NullPointerException
    }

    def 'a specific configuration that defines resourceIncludePath then fileIncludePath throws exception'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .charset(StandardCharsets.US_ASCII)
                        .resourceIncludePath('includePath')
                        .fileIncludePath("otherIncludePath")
        )

        then:
        thrown IllegalStateException
    }

    def 'a specific configuration that defines fileIncludePath then resourceIncludePath throws exception'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .charset(StandardCharsets.US_ASCII)
                        .fileIncludePath("otherIncludePath")
                        .resourceIncludePath('includePath')
        )

        then:
        thrown IllegalStateException
    }

    def 'custom providers can be added'() {
        when:
        def providerA = new TestCustomProvider("providerA")
        def providerB = new TestCustomProvider("providerB")
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .addCustomProvider(providerB)
                        .addCustomProvider(providerA)
        )

        then:
        bundle.configuration.customProviders.containsAll([providerA, providerB])
        bundle.configuration.customProviders.size() == 4
    }

    def 'a specific configuration with a model factory can be applied'() {
        when:
        def bundleConfiguration = new TemplateConfigBundleConfiguration()
            .charset(StandardCharsets.US_ASCII)
            .resourceIncludePath('includePath')
        def oldFactory = bundleConfiguration.dataModelFactory()
        def bundle = new TemplateConfigBundle(bundleConfiguration.dataModelFactory(oldFactory))

        then:
        bundle.configuration.charset == StandardCharsets.US_ASCII
        bundle.configuration.resourceIncludePath == 'includePath'
    }

    def 'a specific configuration with a null model factory throws exception'() {
        when:
        def bundleConfiguration = new TemplateConfigBundleConfiguration()
            .charset(StandardCharsets.US_ASCII)
            .resourceIncludePath('includePath')
        new TemplateConfigBundle(bundleConfiguration.dataModelFactory(null))

        then:
        thrown NullPointerException
    }

    def 'a specific configuration with an output path can be applied'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .outputPath('outputPath')
        )

        then:
        bundle.configuration.outputPath().get() == 'outputPath'
    }

    def 'a specific configuration with a null output path throws exception'() {
        when:
        def bundle = new TemplateConfigBundle(
                new TemplateConfigBundleConfiguration()
                        .outputPath(null)
        )

        then:
        thrown NullPointerException
    }
}
