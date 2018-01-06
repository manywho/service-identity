package com.manywho.services.identity.authorization;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.groups.GroupRepository;
import com.manywho.services.identity.users.User;
import com.manywho.services.identity.users.UserRepository;

import javax.inject.Inject;
import java.util.ArrayList;
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

    public List<Group> findAllGroups(ServiceConfiguration configuration) {
        return groupRepository.findAllByTenant(configuration, new ListFilter());
    }

    public List<User> findAllUsers(ServiceConfiguration configuration) {
        return new ArrayList<>(userRepository.findAllByTenant(configuration, new ListFilter()));
    }

    public List<UUID> findGroupsForUser(ServiceConfiguration configuration, UUID user) {
        return userRepository.findGroups(configuration, user);
    }

    public User findUser(ServiceConfiguration configuration, String id) {
        return userRepository.find(configuration, id);
    }

    public Group findGroup(ServiceConfiguration configuration, String id) {
        return groupRepository.find(configuration, id);
    }
}
