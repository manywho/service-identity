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
    public Group create(ServiceConfiguration configuration, Group object) {
        return null;
    }

    @Override
    public List<Group> create(ServiceConfiguration configuration, List<Group> objects) {
        return null;
    }

    @Override
    public void delete(ServiceConfiguration configuration, Group object) {

    }

    @Override
    public void delete(ServiceConfiguration configuration, List<Group> objects) {

    }

    @Override
    public Group update(ServiceConfiguration configuration, Group group) {
        // TODO: Properly work out how scope to the Tenant
        repository.update(UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"), group);

        return group;
    }

    @Override
    public List<Group> update(ServiceConfiguration configuration, List<Group> objects) {
        return null;
    }

    @Override
    public Group find(ServiceConfiguration configuration, String id) {
        return null;
    }

    @Override
    public List<Group> findAll(ServiceConfiguration configuration, ListFilter filter) {
        // TODO: Properly work out how scope to the Tenant
        return repository.findAllByTenant(UUID.fromString("ad5348c9-df8b-462e-9f9d-492bff5a9468"), filter);
    }
}
