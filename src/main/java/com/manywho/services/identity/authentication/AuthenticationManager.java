package com.manywho.services.identity.authentication;

import com.manywho.sdk.api.security.AuthenticatedWhoResult;
import com.manywho.sdk.api.security.AuthenticationCredentials;
import com.manywho.services.identity.users.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.inject.Inject;
import java.util.UUID;

public class AuthenticationManager {
    private final AuthenticationRepository repository;

    @Inject
    public AuthenticationManager(AuthenticationRepository repository) {
        this.repository = repository;
    }

    public AuthenticatedWhoResult authenticate(AuthenticationCredentials credentials) {
        // See if the user exists by the given email address
        User user = repository.findUserByEmail(credentials.getUsername(), UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"));
//        User user = repository.findUserByEmail(credentials.getUsername(), credentials.getTenantId());
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
            authenticatedWhoResult.setIdentityProvider("?!?!?!?!!?");
            authenticatedWhoResult.setLastName(user.getLastName());
            authenticatedWhoResult.setStatus(AuthenticatedWhoResult.AuthenticationStatus.Authenticated);
            authenticatedWhoResult.setTenantName("??!?!?!?");
            authenticatedWhoResult.setToken(UUID.randomUUID().toString());
            authenticatedWhoResult.setUserId(user.getId().toString());
            authenticatedWhoResult.setUsername(user.getEmail());

            return authenticatedWhoResult;
        }

        return AuthenticatedWhoResult.createDeniedResult();
    }
}
