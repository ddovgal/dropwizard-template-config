package com.expediagroup.dropwizard.bundle.configuration.freemarker

import org.apache.commons.io.IOUtils
import spock.lang.Shared
import spock.lang.Specification

class IncludeSpec extends Specification {

    @Shared
    def TestCustomProvider environmentProvider = TestCustomProvider.forEnv()

    @Shared
    def TemplateConfigurationSourceProvider resourceIncludeProvider = new TemplateConfigurationSourceProvider(
            new TestConfigSourceProvider(),
            new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), environmentProvider)
                    .resourceIncludePath('/config-snippets')
    )

    @Shared
    def TemplateConfigurationSourceProvider fileIncludeProvider = new TemplateConfigurationSourceProvider(
            new TestConfigSourceProvider(),
            new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), environmentProvider)
                    .fileIncludePath('src/test/resources/config-snippets/'),
    )

    def 'config snippets can be included from the classpath and filesystem'() {
        given:
        def config = '''
                server:
                  type: simple
                  connector:
                    type: http
                    port: 8080

                <#include "database.yaml">

                logging:
                  level: WARN
                '''.stripIndent()

        expect:
        def parsedConfig = provider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig).replace("\r", "")
        parsedConfigAsString == '''
                server:
                  type: simple
                  connector:
                    type: http
                    port: 8080

                database:
                  driverClass: org.postgresql.Driver
                  user: my-app
                  password: secret
                  url: jdbc:postgresql://localhost:5432/my-app-db

                logging:
                  level: WARN
                '''.stripIndent()

        where:
        provider << [resourceIncludeProvider, fileIncludeProvider]
    }

    def 'config snippets can use templating features'() {
        given:
        def config = '''
                <#include "database-with-templating.yaml">
                '''.stripIndent()

        environmentProvider.putVariable('DB_USER', 'my-app')
        environmentProvider.putVariable('DB_PASSWORD', 'secret')
        environmentProvider.putVariable('DB_HOST', 'localhost')
        environmentProvider.putVariable('DB_PORT', '5432')

        expect:
        def parsedConfig = provider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig).replace("\r", "")
        parsedConfigAsString == '''
                database:
                  driverClass: org.postgresql.Driver
                  user: my-app
                  password: secret
                  url: jdbc:postgresql://localhost:5432/my-app-db
                '''.stripIndent()

        where:
        provider << [resourceIncludeProvider, fileIncludeProvider]
    }

    def 'relative resource include paths will be interpreted as absolute'() {
        given:
        def relativeIncludePath = 'config-snippets'
        def TemplateConfigurationSourceProvider provider = new TemplateConfigurationSourceProvider(
                new TestConfigSourceProvider(),
                new TemplateConfigBundleConfiguration().resourceIncludePath(relativeIncludePath)
        )
        def config = '''
                <#include "database.yaml">
                '''.stripIndent()

        when:
        def parsedConfig = provider.open(config)

        then:
        def parsedConfigAsString = IOUtils.toString(parsedConfig).replace("\r", "")
        parsedConfigAsString == '''
                database:
                  driverClass: org.postgresql.Driver
                  user: my-app
                  password: secret
                  url: jdbc:postgresql://localhost:5432/my-app-db
                '''.stripIndent()
    }

    def 'specifying file and then resource include paths fails'() {
        given:
        def TemplateConfigBundleConfiguration config =
                new TemplateConfigBundleConfiguration()
                        .fileIncludePath("src/test/resources/config-snippets/")
        when:
        config.resourceIncludePath("/config-snippets")

        then:
        thrown(IllegalStateException)
    }

    def 'specifying resource and then file include paths fails'() {
        given:
        def TemplateConfigBundleConfiguration config =
                new TemplateConfigBundleConfiguration()
                        .resourceIncludePath("/config-snippets")

        when:
        config.fileIncludePath("src/test/resources/config-snippets/")

        then:
        thrown(IllegalStateException)
    }
}
