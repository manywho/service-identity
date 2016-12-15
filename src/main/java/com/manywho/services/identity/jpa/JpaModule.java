package com.manywho.services.identity.jpa;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import java.util.Map;

public class JpaModule extends AbstractModule {
    @Override
    protected void configure() {
        Map<String, Object> properties = Maps.newHashMap();

        Map<String, String> environmentVariables = System.getenv();

        for (String variable : environmentVariables.keySet()) {
            if (variable.equals("DATABASE_URL")) {
                properties.put("javax.persistence.jdbc.url", environmentVariables.get(variable));
            }

            if (variable.equals("DATABASE_USER")) {
                properties.put("javax.persistence.jdbc.user", environmentVariables.get(variable));
            }

            if (variable.equals("DATABASE_PASSWORD")) {
                properties.put("javax.persistence.jdbc.password", environmentVariables.get(variable));
            }
        }

        JpaPersistModule jpaPersistModule = new JpaPersistModule("default");
        jpaPersistModule.properties(properties);

        install(jpaPersistModule);

        bind(JpaInitializer.class).asEagerSingleton();
    }
}
