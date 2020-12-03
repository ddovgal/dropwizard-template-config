package com.expediagroup.dropwizard.bundle.configuration.freemarker

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class AdditionalFreemarkerFeaturesSpec extends Specification {

    TestCustomProvider environmentProvider = TestCustomProvider.forEnv()

    TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(),
                    new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), environmentProvider))

    def 'conditionally enable https - on'() {
        given:
        def config = '''
                server:
                  applicationConnectors:
                    - type: http
                      port: ${env.PORT!8080}
                <#if env.ENABLE_SSL == 'true'>
                    - type: https
                      port: ${env.SSL_PORT!8443}
                      keyStorePath: ${env.SSL_KEYSTORE_PATH}
                      keyStorePassword: ${env.SSL_KEYSTORE_PASS}
                </#if>
                '''

        environmentProvider.putVariable('ENABLE_SSL', 'true')
        environmentProvider.putVariable('SSL_KEYSTORE_PATH', 'example.keystore')
        environmentProvider.putVariable('SSL_KEYSTORE_PASS', 'secret')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('- type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
        assertThat(parsedConfigAsString).contains('- type: https')
        assertThat(parsedConfigAsString).contains('port: 8443')
        assertThat(parsedConfigAsString).contains('keyStorePath: example.keystore')
        assertThat(parsedConfigAsString).contains('keyStorePassword: secret')
    }

    def 'conditionally enable https - off'() {
        given:
        def config = '''
                server:
                  applicationConnectors:
                    - type: http
                      port: ${env.PORT!8080}
                <#if env.ENABLE_SSL == 'true'>
                    - type: https
                      port: ${env.SSL_PORT!8443}
                      keyStorePath: ${env.SSL_KEYSTORE_PATH}
                      keyStorePassword: ${env.SSL_KEYSTORE_PASS}
                </#if>
                '''

        environmentProvider.putVariable('ENABLE_SSL', 'false')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('- type: http')
        assertThat(parsedConfigAsString).contains('port: 8080')
    }

    def 'comments can be used'(){
        given:
        def config = '''
                server:
                  applicationConnectors:
                    - type: http
                      port: ${PORT!8080}
                <#-- Un-comment to enable HTTPS
                    - type: https
                      port: ${SSL_PORT!8443}
                      keyStorePath: ${SSL_KEYSTORE_PATH}
                      keyStorePassword: ${SSL_KEYSTORE_PASS}
                -->
                '''

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).doesNotContain('Un-comment to enable HTTPS')
        assertThat(parsedConfigAsString).doesNotContain('- type: https')
        assertThat(parsedConfigAsString).doesNotContain('keyStorePassword:')
    }

    def 'simulating application profiles - production profile'() {
        given:
        def config = '''
                logging:
                <#if env.PROFILE == 'production'>
                  level: WARN
                  loggers:
                    com.example.my_app: INFO
                    org.hibernate.SQL: OFF
                  appenders:
                    - type: syslog
                      host: localhost
                      facility: local0
                <#elseif env.PROFILE == 'development'>
                  level: INFO
                  loggers:
                    com.example.my_app: DEBUG
                    org.hibernate.SQL: DEBUG
                  appenders:
                    - type: console
                </#if>
                '''

        environmentProvider.putVariable('PROFILE', 'production')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('level: WARN')
        assertThat(parsedConfigAsString).contains('com.example.my_app: INFO')
        assertThat(parsedConfigAsString).contains('org.hibernate.SQL: OFF')
        assertThat(parsedConfigAsString).contains('- type: syslog')
        assertThat(parsedConfigAsString).contains('host: localhost')
        assertThat(parsedConfigAsString).contains('facility: local0')
    }

    def 'simulating application profiles - development profile'() {
        given:
        def config = '''
                logging:
                <#if env.PROFILE == 'production'>
                  level: WARN
                  loggers:
                    com.example.my_app: INFO
                    org.hibernate.SQL: OFF
                  appenders:
                    - type: syslog
                      host: localhost
                      facility: local0
                <#elseif env.PROFILE == 'development'>
                  level: INFO
                  loggers:
                    com.example.my_app: DEBUG
                    org.hibernate.SQL: DEBUG
                  appenders:
                    - type: console
                </#if>
                '''

        environmentProvider.putVariable('PROFILE', 'development')

        when:
        def parsedConfig = templateConfigurationSourceProvider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('level: INFO')
        assertThat(parsedConfigAsString).contains('com.example.my_app: DEBUG')
        assertThat(parsedConfigAsString).contains('org.hibernate.SQL: DEBUG')
        assertThat(parsedConfigAsString).contains('- type: console')
    }

}
