package com.expediagroup.dropwizard.bundle.configuration.freemarker

import freemarker.core.InvalidReferenceException
import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class SystemPropertiesSpec extends Specification {

    def TestCustomProvider systemPropertiesProvider = TestCustomProvider.forSys()

    def TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(systemPropertiesProvider, Providers.fromEnvironmentProperties()))

    def 'replacing a system property works'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys.http_port}'''

        systemPropertiesProvider.putVariable('http_port', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'using a missing system property honors default value'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys.http_port!8080}'''

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'using a missing system property without default value fails'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys.http_port}'''

        when:
        templateConfigurationSourceProvider.open(config)

        then:
        def exception = thrown(IllegalStateException)
        def exceptionsCause = exception.cause
        assertThat(exceptionsCause).isInstanceOf(InvalidReferenceException)
    }

    def 'can use sys prefix'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys.http_port}'''

        systemPropertiesProvider.putVariable('http_port', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'referencing a system property with a dot in its name must use bracket syntax'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys['my_app.http.port']}'''

        systemPropertiesProvider.putVariable('my_app.http.port', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'can use backslash for names with dash'() throws Exception {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${sys.http\\-port}'''

        systemPropertiesProvider.putVariable('http-port', '8080')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

}
