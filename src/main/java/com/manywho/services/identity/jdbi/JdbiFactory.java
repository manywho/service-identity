package com.manywho.services.identity.jdbi;

import com.manywho.services.identity.ServiceConfiguration;
import org.jdbi.v3.core.Jdbi;

public class JdbiFactory {
    public Jdbi create(ServiceConfiguration configuration) {
        String url;

        switch (configuration.getDatabaseType()) {
            case "postgresql":
                url = String.format("jdbc:postgresql://%s:%d/%s", configuration.getDatabaseHostname(), configuration.getDatabasePort(), configuration.getDatabaseName());
                break;
            default:
                throw new RuntimeException("The database type " + configuration.getDatabaseType() + " is not supported");
        }

        return Jdbi.create(url, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
    }
}
