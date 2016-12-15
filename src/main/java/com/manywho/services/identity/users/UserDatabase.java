package com.manywho.services.identity.users;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.Database;
import com.manywho.services.identity.ServiceConfiguration;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class UserDatabase implements Database<ServiceConfiguration, User> {
    private final UserRepository repository;

    @Inject
    public UserDatabase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(ServiceConfiguration configuration, User user) {
        UUID tenant = UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468");

        // Check if a user already exists with that email
        if (repository.existsByEmail(tenant, user.getEmail())) {
            throw new RuntimeException("A user already exists with that email address!");
        }

        user.setId(UUID.randomUUID());

        // TODO: Properly work out how scope to the Tenant
        repository.create(tenant, user);

        return user;
    }

    @Override
    public List<User> create(ServiceConfiguration configuration, List<User> list) {
        return null;
    }

    @Override
    public void delete(ServiceConfiguration configuration, User user) {
        // TODO: Properly work out how scope to the Tenant
        repository.delete(UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"), user);
    }

    @Override
    public void delete(ServiceConfiguration configuration, List<User> list) {

    }

    @Override
    public User update(ServiceConfiguration configuration, User user) {
        // Only update the password if one was given
        if (user.getPassword() != null || !user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        // TODO: Properly work out how scope to the Tenant
        repository.update(UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"), user);

        return user;
    }

    @Override
    public List<User> update(ServiceConfiguration configuration, List<User> list) {
        return null;
    }

    @Override
    public User find(ServiceConfiguration configuration, String s) {
        return null;
    }

    @Override
    public List<User> findAll(ServiceConfiguration configuration, ListFilter listFilter) {
        // TODO: Properly work out how scope to the Tenant
        return repository.findAllByTenant(UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"), new ListFilter());
    }
}
