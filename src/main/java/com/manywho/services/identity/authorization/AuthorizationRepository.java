package com.manywho.services.identity.authorization;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.groups.GroupRepository;
import com.manywho.services.identity.users.User;
import com.manywho.services.identity.users.UserRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class AuthorizationRepository {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Inject
    public AuthorizationRepository(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public List<Group> findAllGroups(UUID tenant) {
        return groupRepository.findAllByTenant(tenant, new ListFilter());
    }

    public List<User> findAllUsers(UUID tenant) {
        return userRepository.findAllByTenant(tenant, new ListFilter());
    }

    public List<UUID> findGroupsForUser(UUID user) {
        return userRepository.findGroups(user);
    }
}
