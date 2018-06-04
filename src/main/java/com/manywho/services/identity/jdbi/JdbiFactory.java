package com.manywho.services.identity.jdbi;

import com.google.common.base.Strings;
import com.manywho.services.identity.ServiceConfiguration;
import org.jdbi.v3.core.Jdbi;

public class JdbiFactory {
    public Jdbi create(ServiceConfiguration configuration) {
        String url;

        switch (configuration.getDatabaseType()) {
            case "postgresql":
                String schema = Strings.isNullOrEmpty(configuration.getDatabaseSchema())
                        ? "public"
                        : configuration.getDatabaseSchema();

                String sslMode = configuration.isDatabaseSsl()
                        ? "require"
                        : null;

                if(sslMode != null)
                    url = String.format("jdbc:postgresql://%s:%d/%s?currentSchema=%s&sslmode=%s", configuration.getDatabaseHostname(), configuration.getDatabasePort(), configuration.getDatabaseName(), schema, sslMode);
                else
                    url = String.format("jdbc:postgresql://%s:%d/%s?currentSchema=%s", configuration.getDatabaseHostname(), configuration.getDatabasePort(), configuration.getDatabaseName(), schema);
                break;
            default:
                throw new RuntimeException("The database type " + configuration.getDatabaseType() + " is not supported");
        }

        return Jdbi.create(url, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
    }
}
