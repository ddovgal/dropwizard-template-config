package com.expediagroup.dropwizard.bundle.configuration.freemarker

import com.google.common.io.Files
import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.assertj.core.api.Assertions.assertThat

class OutputPathSpec extends Specification {
    TestCustomProvider environmentProvider = TestCustomProvider.forEnv()

    def outputPath = System.getProperty('java.io.tmpdir') + '/outputPathSpec.yml'

    TemplateConfigurationSourceProvider provider = new TemplateConfigurationSourceProvider(
            new TestConfigSourceProvider(),
            new TemplateConfigBundleConfiguration(Providers.fromSystemProperties(), environmentProvider)
                    .outputPath(outputPath),

    )

    def 'rendered output is written to configured outputPath'() {
        given:
        def config = '''
                server:
                  applicationConnectors:
                    - type: http
                      port: ${PORT!8080}
                '''

        when:
        def parsedConfig = provider.open(config)
        def parsedConfigAsString = IOUtils.toString(parsedConfig, StandardCharsets.UTF_8)
        def configOnDiskAsString = Files.asCharSource(new File(outputPath), StandardCharsets.UTF_8).read()

        then:
        assertThat(parsedConfigAsString).isEqualTo(configOnDiskAsString)
        assertThat(configOnDiskAsString).contains('port: 8080')

        cleanup:
        new File(outputPath).delete()
    }
}
