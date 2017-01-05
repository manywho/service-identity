package com.manywho.services.identity.jpa;

import com.google.common.collect.Maps;
import com.manywho.services.identity.ServiceConfiguration;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;

public class JpaFactory {
    public JPAQueryFactory createQueryFactory(ServiceConfiguration configuration) {
        return new JPAQueryFactory(createEntityManager(configuration));
    }

    public EntityManager createEntityManager(ServiceConfiguration configuration) {
        String url;

        switch (configuration.getDatabaseType()) {
            case "postgresql":
                url = String.format("jdbc:postgresql://%s:%d/%s", configuration.getDatabaseHostname(), configuration.getDatabasePort(), configuration.getDatabaseName());
                break;
            default:
                throw new RuntimeException("The database type " + configuration.getDatabaseType() + " is not supported");
        }

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("javax.persistence.jdbc.url", url);
        properties.put("javax.persistence.jdbc.user", configuration.getDatabaseUsername());
        properties.put("javax.persistence.jdbc.password", configuration.getDatabasePassword());

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("default", properties);

        return factory.createEntityManager();
    }
}
