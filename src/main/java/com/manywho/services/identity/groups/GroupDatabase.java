package com.manywho.services.identity.groups;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.Database;
import com.manywho.services.identity.ServiceConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class GroupDatabase implements Database<ServiceConfiguration, Group> {
    private final GroupRepository repository;

    @Inject
    public GroupDatabase(GroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Group create(ServiceConfiguration configuration, Group group) {
        // Check if a group already exists with that name
        if (repository.existsByName(configuration, group.getName())) {
            throw new RuntimeException("A group already exists with that name!");
        }

        group.setId(UUID.randomUUID());

        repository.create(configuration, group);

        return group;
    }

    @Override
    public List<Group> create(ServiceConfiguration configuration, List<Group> objects) {
        throw new RuntimeException("Creating multiple groups at once isn't supported yet");
    }

    @Override
    public void delete(ServiceConfiguration configuration, Group object) {
        throw new RuntimeException("Deleting groups isn't supported yet");
    }

    @Override
    public void delete(ServiceConfiguration configuration, List<Group> objects) {
        throw new RuntimeException("Deleting multiple groups at once isn't supported yet");
    }

    @Override
    public Group update(ServiceConfiguration configuration, Group group) {
        repository.update(configuration, group);

        return group;
    }

    @Override
    public List<Group> update(ServiceConfiguration configuration, List<Group> objects) {
        throw new RuntimeException("Updating multiple groups at once isn't supported yet");
    }

    @Override
    public Group find(ServiceConfiguration configuration, String id) {
        throw new RuntimeException("Loading a single user isn't supported yet");
    }

    @Override
    public List<Group> findAll(ServiceConfiguration configuration, ListFilter filter) {
        return repository.findAllByTenant(configuration, filter);
    }
}
