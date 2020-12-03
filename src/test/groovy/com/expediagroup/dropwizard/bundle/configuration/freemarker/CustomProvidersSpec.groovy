package com.expediagroup.dropwizard.bundle.configuration.freemarker

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class CustomProvidersSpec extends Specification {

    def TestCustomProvider environmentProvider = TestCustomProvider.forEnv()
    def TestCustomProvider systemPropertiesProvider = TestCustomProvider.forSys()
    def TestCustomProvider customProviderA = new TestCustomProvider("providerA")
    def TestCustomProvider customProviderB = new TestCustomProvider("providerB")
    def TemplateConfigBundleConfiguration templateConfigBundleConfiguration =
            new TemplateConfigBundleConfiguration(systemPropertiesProvider, environmentProvider)
            .addCustomProvider(customProviderA)
            .addCustomProvider(customProviderB)

    def TemplateConfigurationSourceProvider templateConfigurationSourceProvider =
            new TemplateConfigurationSourceProvider(new TestConfigSourceProvider(), templateConfigBundleConfiguration)

    def 'replacing custom variables inline works'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${providerA.DB_USER}
                          password: ${providerB.DB_PASSWORD}
                          url: jdbc:postgresql://${providerA.DB_HOST}:${providerB.DB_PORT}/my-app-db'''
        customProviderA.putVariable('DB_USER', 'user')
        customProviderB.putVariable('DB_PASSWORD', 'password')
        customProviderA.putVariable('DB_HOST', 'db-host')
        customProviderB.putVariable('DB_PORT', '12345')

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

    def 'custom variables have precedence over environment variables'() {
        given:
        def config = '''database:
                          driverClass: org.postgresql.Driver
                          user: ${providerA.DB_USER}
                          password: ${providerB.DB_PASSWORD}
                          url: jdbc:postgresql://${providerA.DB_HOST}:${providerB.DB_PORT}/my-app-db'''
        environmentProvider.putVariable('DB_USER', 'bad_user')
        customProviderA.putVariable('DB_USER', 'good_user')
        customProviderB.putVariable('DB_PASSWORD', 'password')
        customProviderA.putVariable('DB_HOST', 'db-host')
        customProviderB.putVariable('DB_PORT', '12345')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('database:')
        assertThat(parsedConfigAsString).contains('driverClass: org.postgresql.Driver')
        assertThat(parsedConfigAsString).contains('user: good_user')
        assertThat(parsedConfigAsString).contains('password: password')
        assertThat(parsedConfigAsString).contains('url: jdbc:postgresql://db-host:12345/my-app-db')
    }

    def 'parses json string correctly'() {
        given:
        def config = '''my_keys:
                          <#assign my_keys = providerA.my_keys?eval>
                          <#list my_keys?keys as my_key>
                          ${my_key}: ${my_keys[my_key]}
                          </#list>'''
        customProviderA.putVariable('my_keys', '{ "key1": "secret1", "key2": "secret2" } ')

        when:
        InputStream parsedConfig = templateConfigurationSourceProvider.open(config)
        String parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)

        then:
        assertThat(parsedConfigAsString).contains('my_keys:')
        assertThat(parsedConfigAsString).contains('  key1: secret1')
        assertThat(parsedConfigAsString).contains('  key2: secret2')
    }

}
