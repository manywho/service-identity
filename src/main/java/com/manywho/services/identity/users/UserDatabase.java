package com.manywho.services.identity.users;

import com.manywho.sdk.api.draw.content.Command;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.api.run.elements.type.MObject;
import com.manywho.sdk.api.run.elements.type.ObjectDataType;
import com.manywho.sdk.services.database.Database;
import com.manywho.services.identity.ServiceConfiguration;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDatabase implements Database<ServiceConfiguration, User> {
    private final UserRepository repository;

    @Inject
    public UserDatabase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User find(ServiceConfiguration configuration, ObjectDataType objectDataType, Command command, String id) {
        throw new RuntimeException("Loading a single user isn't supported yet");
    }

    @Override
    public List<User> findAll(ServiceConfiguration configuration, ObjectDataType objectDataType, Command command, ListFilter filter, List<MObject> objects) {
        return new ArrayList<>(repository.findAllByTenant(configuration, filter));
    }

    @Override
    public User create(ServiceConfiguration configuration, ObjectDataType objectDataType, User user) {
        // Check if a user already exists with that email
        if (repository.existsByEmail(configuration, user.getEmail())) {
            throw new RuntimeException("A user already exists with that email address!");
        }

        user.setId(UUID.randomUUID());

        // Only update the password if one was given
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // We need to set this to null because empty strings are sent in for "no value" 🙄
            user.setPassword(null);
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        repository.create(configuration, user);

        return user;
    }

    @Override
    public List<User> create(ServiceConfiguration configuration, ObjectDataType objectDataType, List<User> objects) {
        throw new RuntimeException("Creating multiple users at once isn't supported yet");
    }

    @Override
    public void delete(ServiceConfiguration configuration, ObjectDataType objectDataType, User user) {
        repository.delete(configuration, user);
    }

    @Override
    public void delete(ServiceConfiguration configuration, ObjectDataType objectDataType, List<User> objects) {
        throw new RuntimeException("Deleting multiple users at once isn't supported yet");
    }

    @Override
    public User update(ServiceConfiguration configuration, ObjectDataType objectDataType, User user) {
        // Only update the password if one was given
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // We need to set this to null because empty strings are sent in for "no value" 🙄
            user.setPassword(null);
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        repository.update(configuration, user);

        return user;
    }

    @Override
    public List<User> update(ServiceConfiguration configuration, ObjectDataType objectDataType, List<User> objects) {
        throw new RuntimeException("Updating multiple users at once isn't supported yet");
    }
}
