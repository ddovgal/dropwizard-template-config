package com.expediagroup.dropwizard.bundle.configuration.freemarker;

import java.util.HashMap;
import java.util.Map;

public class TestCustomProvider implements TemplateConfigVariablesProvider {
    private final String namespace;
    private final Map<String, String> data = new HashMap<>();

    public TestCustomProvider(String namespace) {
        this.namespace = namespace;
    }

    public void putVariable(String name, String value) {
        data.put(name, value);
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public Map<String, String> getVariables() {
        return this.data;
    }

    public static TestCustomProvider forSys() {
        return new TestCustomProvider("sys");
    }

    public static TestCustomProvider forEnv() {
        return new TestCustomProvider("env");
    }
}
