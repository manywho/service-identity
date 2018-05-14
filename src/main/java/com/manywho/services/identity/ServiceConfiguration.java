package com.manywho.services.identity;

import com.manywho.sdk.api.ContentType;
import com.manywho.sdk.services.configuration.Configuration;

public class ServiceConfiguration implements Configuration {
    @Configuration.Setting(name = "Database Type", contentType = ContentType.String)
    private String databaseType;

    @Configuration.Setting(name = "Database Hostname", contentType = ContentType.String)
    private String databaseHostname;

    @Configuration.Setting(name = "Database Port", contentType = ContentType.Number)
    private Integer databasePort;

    @Configuration.Setting(name = "Database Username", contentType = ContentType.String)
    private String databaseUsername;

    @Configuration.Setting(name = "Database Password", contentType = ContentType.Password)
    private String databasePassword;

    @Configuration.Setting(name = "Database Name", contentType = ContentType.String)
    private String databaseName;

    @Configuration.Setting(name = "Database Schema", contentType = ContentType.String, required = false)
    private String databaseSchema;

    @Configuration.Setting(name = "Database SSL?", contentType = ContentType.Boolean, required = false)
    private boolean databaseSsl;

    public String getDatabaseType() {
        return databaseType;
    }

    public String getDatabaseHostname() {
        return databaseHostname;
    }

    public Integer getDatabasePort() {
        return databasePort;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public boolean isDatabaseSsl() {
        return databaseSsl;
    }
}
