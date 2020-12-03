package com.expediagroup.dropwizard.bundle.configuration.freemarker

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class AdvancedInterpolationSpec extends Specification {

    TestCustomProvider environmentProvider = TestCustomProvider.forEnv()
    TestCustomProvider systemPropertiesProvider = TestCustomProvider.forSys()

    TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(systemPropertiesProvider, environmentProvider))

    def 'replacing an environment variable inline works'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${env.DB_USER}
                          password: ${env.DB_PASSWORD}
                          url: jdbc:postgresql://${env.DB_HOST}:${env.DB_PORT}/my-app-db'''

        environmentProvider.putVariable('DB_USER', 'user')
        environmentProvider.putVariable('DB_PASSWORD', 'password')
        environmentProvider.putVariable('DB_HOST', 'db-host')
        environmentProvider.putVariable('DB_PORT', '12345')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('database:')
        assertThat(parsedConfigAsString).contains('driverClass: org.postgresql.Driver')
        assertThat(parsedConfigAsString).contains('user: user')
        assertThat(parsedConfigAsString).contains('password: password')
        assertThat(parsedConfigAsString).contains('url: jdbc:postgresql://db-host:12345/my-app-db')
    }

    def 'inserting whole mappings works'() {
        given:
        def config = '''
                server:
                  ${env.SERVER_TYPE_LINE}
                  connector:
                    ${env.SERVER_CONNECTOR_TYPE_LINE}
                    port: 8080
                '''

        environmentProvider.putVariable('SERVER_TYPE_LINE', 'type: simple')
        environmentProvider.putVariable('SERVER_CONNECTOR_TYPE_LINE', 'type: http')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('type: simple')
        assertThat(parsedConfigAsString).contains('type: http')
    }

    def 'environment variables have precedence over system properties'() {
        given:
        def config = '''server:
                          type: simple
                          connector:
                            type: http
                            port: ${env.port}'''

        environmentProvider.putVariable('port', '8080')
        systemPropertiesProvider.putVariable('port', '8081')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('server:')
        assertThat(parsedConfigAsString).contains('type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

}
