package com.expediagroup.dropwizard.bundle.configuration.freemarker

import freemarker.core.InvalidReferenceException
import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class EnvironmentVariablesSpec extends Specification {

    TestCustomProvider environmentProvider = TestCustomProvider.forEnv()

    TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), environmentProvider))

    def 'replacing an environment variable works'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.PORT}'''

        environmentProvider.putVariable('PORT', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'using a missing environment variable honors default value'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.PORT!8080}'''

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'using a missing environment variable without default value fails'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.PORT}'''

        when:
        templateConfigurationSourceProvider.open(config)

        then:
        def exception = thrown(IllegalStateException)
        def exceptionsCause = exception.cause
        assertThat(exceptionsCause).isInstanceOf(InvalidReferenceException)
    }

    def 'can use env prefix'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.PORT}'''

        environmentProvider.putVariable('PORT', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }


}
