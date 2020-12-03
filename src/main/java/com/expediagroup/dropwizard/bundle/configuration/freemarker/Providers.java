/*
Copyright 2020 Expedia, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class with creation methods for commonly used {@link TemplateConfigVariablesProvider}
 */
public class Providers {

    /**
     * Namespace for environment variables
     */
    public static final String ENV_NAMESPACE = "env";

    /**
     * Namespace for system variables
     */
    public static final String SYS_NAMESPACE = "sys";

    private Providers() {
    }

    /**
     * Creates {@link TemplateConfigVariablesProvider} with map of system properties
     *
     * @return provider with system properties
     */
    public static TemplateConfigVariablesProvider fromSystemProperties() {
        return fromProperties(SYS_NAMESPACE, System.getProperties());
    }

    /**
     * Creates {@link TemplateConfigVariablesProvider} with map of environment properties
     *
     * @return provider with environment properties
     */
    public static TemplateConfigVariablesProvider fromEnvironmentProperties() {
        return new MapVariablesProvider(ENV_NAMESPACE, System.getenv());
    }

    /**
     * Wraps a map into a {@link TemplateConfigVariablesProvider} with given namespace
     *
     * @param namespace to use for provider
     * @param variables to wrap into provider
     * @return provider with given namespace and map
     */
    public static TemplateConfigVariablesProvider fromMap(String namespace, Map<String, String> variables) {
        return new MapVariablesProvider(namespace, variables);
    }

    /**
     * Converts properties into a {@link TemplateConfigVariablesProvider} with given namespace
     *
     * @param namespace to use for provider
     * @param properties to wrap into provider
     * @return provider with given namespace and map
     */
    public static TemplateConfigVariablesProvider fromProperties(String namespace, Properties properties) {
        return fromMap(namespace, properties.stringPropertyNames()
            .stream()
            .collect(Collectors.toMap(Function.identity(), properties::getProperty)));
    }

    /**
     * Converts properties into a {@link TemplateConfigVariablesProvider} with given namespace
     *
     * @param namespace to use for provider
     * @param propertiesURL URL to properties to wrap into provider
     * @return provider with given namespace and map
     * @throws IOException processing properties
     */
    public static TemplateConfigVariablesProvider fromProperties(String namespace, URL propertiesURL) throws IOException {
        Properties properties = new Properties();
        properties.load(propertiesURL.openStream());
        return fromProperties(namespace, properties);
    }

    /**
     * Merges two providers into a new provider with combined variables. Right provider may override variables of left
     * provider
     *
     * @param left variables provider
     * @param right variables provider
     * @return new provider with merged variables
     */
    public static TemplateConfigVariablesProvider mergeProviders(TemplateConfigVariablesProvider left,
        TemplateConfigVariablesProvider right) {
        Map<String, String> mergedVariables = new HashMap<>();
        mergedVariables.putAll(left.getVariables());
        mergedVariables.putAll(right.getVariables());

        return fromMap(right.getNamespace(), mergedVariables);
    }

    /**
     * Adapting variables from {@link Map} and {@link Properties}.
     * Namespace cannot be blank and variables cannot be null
     */
    private static class MapVariablesProvider implements TemplateConfigVariablesProvider {

        private final String namespace;

        private final Map<String, String> variables;

        /**
         * Instantiate provider with map as a source
         *
         * @param namespace name space to return out of the provider
         * @param variables Map of variables to return out the provider
         */
        private MapVariablesProvider(String namespace, Map<String, String> variables) {
            this.namespace = namespace;
            if (namespace.trim().isEmpty()) {
                throw new IllegalArgumentException("Namespace cannot be blank.");
            }
            this.variables = Collections.unmodifiableMap(variables);
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public Map<String, String> getVariables() {
            return variables;
        }

    }
}
