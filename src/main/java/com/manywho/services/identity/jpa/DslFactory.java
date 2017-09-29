package com.manywho.services.identity.jpa;

import com.google.common.collect.Maps;
import com.manywho.services.identity.ServiceConfiguration;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.Map;

public class DslFactory {
    public JPAQueryFactory createJpaQueryFactory(ServiceConfiguration configuration) {
        return new JPAQueryFactory(createEntityManager(configuration));
    }

    public SQLQueryFactory createSqlQueryFactory(ServiceConfiguration configuration) {
        DataSource dataSource;

        switch (configuration.getDatabaseType()) {
            case "postgresql":
                dataSource = new PGSimpleDataSource();
                ((PGSimpleDataSource) dataSource).setServerName(configuration.getDatabaseHostname());
                ((PGSimpleDataSource) dataSource).setDatabaseName(configuration.getDatabaseName());
                ((PGSimpleDataSource) dataSource).setPortNumber(configuration.getDatabasePort());
                ((PGSimpleDataSource) dataSource).setUser(configuration.getDatabaseUsername());
                ((PGSimpleDataSource) dataSource).setPassword(configuration.getDatabasePassword());
                break;
            default:
                throw new RuntimeException("The database type " + configuration.getDatabaseType() + " is not supported");
        }

        return new SQLQueryFactory(new Configuration(new PostgreSQLTemplates()), dataSource);
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
