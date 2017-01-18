package com.manywho.services.identity.authentication;

import com.manywho.sdk.api.security.AuthenticatedWhoResult;
import com.manywho.sdk.api.security.AuthenticationCredentials;
import com.manywho.sdk.services.configuration.ConfigurationParser;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.users.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.inject.Inject;
import java.util.UUID;

public class AuthenticationManager {
    private final AuthenticationRepository repository;
    private final ConfigurationParser configurationParser;

    @Inject
    public AuthenticationManager(AuthenticationRepository repository, ConfigurationParser configurationParser) {
        this.repository = repository;
        this.configurationParser = configurationParser;
    }

    public AuthenticatedWhoResult authenticate(AuthenticationCredentials credentials) {
        ServiceConfiguration configuration = configurationParser.from(credentials);

        // See if the user exists by the given email address
        User user = repository.findUserByEmail(configuration, credentials.getUsername());
        if (user == null || !user.hasPassword()) {
            return AuthenticatedWhoResult.createDeniedResult();
        }

        // Check the given credentials against the database and either authenticate or don't
        if (BCrypt.checkpw(credentials.getPassword(), user.getPassword())) {
            AuthenticatedWhoResult authenticatedWhoResult = new AuthenticatedWhoResult();
            authenticatedWhoResult.setDirectoryId("manywho-identity");
            authenticatedWhoResult.setDirectoryName("ManyWho Identity");
            authenticatedWhoResult.setEmail(user.getEmail());
            authenticatedWhoResult.setFirstName(user.getFirstName());
            authenticatedWhoResult.setIdentityProvider("?");
            authenticatedWhoResult.setLastName(user.getLastName());
            authenticatedWhoResult.setStatus(AuthenticatedWhoResult.AuthenticationStatus.Authenticated);
            authenticatedWhoResult.setTenantName("?");
            authenticatedWhoResult.setToken(UUID.randomUUID().toString());
            authenticatedWhoResult.setUserId(user.getId().toString());
            authenticatedWhoResult.setUsername(user.getEmail());

            return authenticatedWhoResult;
        }

        return AuthenticatedWhoResult.createDeniedResult();
    }
}
