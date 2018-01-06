package com.manywho.services.identity.authorization;

import com.google.common.collect.Lists;
import com.manywho.sdk.api.AuthorizationType;
import com.manywho.sdk.api.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.api.run.elements.type.ObjectDataResponse;
import com.manywho.sdk.api.security.AuthenticatedWho;
import com.manywho.sdk.services.configuration.ConfigurationParser;
import com.manywho.sdk.services.types.TypeBuilder;
import com.manywho.sdk.services.types.system.$User;
import com.manywho.sdk.services.types.system.AuthorizationAttribute;
import com.manywho.sdk.services.types.system.AuthorizationGroup;
import com.manywho.sdk.services.types.system.AuthorizationUser;
import com.manywho.sdk.services.values.ValueParser;
import com.manywho.services.identity.ServiceConfiguration;
import lombok.val;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthorizationManager {
    private final TypeBuilder typeBuilder;
    private final ValueParser valueParser;
    private final ConfigurationParser configurationParser;
    private final AuthorizationRepository repository;

    @Inject
    public AuthorizationManager(TypeBuilder typeBuilder, ValueParser valueParser, ConfigurationParser configurationParser, AuthorizationRepository repository) {
        this.typeBuilder = typeBuilder;
        this.valueParser = valueParser;
        this.configurationParser = configurationParser;
        this.repository = repository;
    }

    public ObjectDataResponse authorize(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        String status = "401";

        ServiceConfiguration configuration = configurationParser.from(request);

        switch (request.getAuthorization().getGlobalAuthenticationType()) {
            case AllUsers:
                // If it's a public user (i.e. not logged in) then return a 401
                if (authenticatedWho.getUserId().equals("PUBLIC_USER")) {
                    status = "401";
                } else {
                    status = "200";
                }

                break;
            case Public:
                status = "200";
                break;
            case Specified:
                if (!authenticatedWho.getUserId().equals("PUBLIC_USER")) {
                    // Get a list of groups that the logged-in user is a member of
                    List<UUID> groups = repository.findGroupsForUser(configuration, UUID.fromString(authenticatedWho.getUserId()));

                    // Check if the logged-in user is allowed to authorize against the flow
                    if (request.getAuthorization().hasUsers()) {
                        if (request.getAuthorization().getUsers().stream()
                                .anyMatch(user -> authenticatedWho.getUserId().equals(user.getAuthenticationId()))) {
                            status = "200";
                        }
                    }

                    // Check if the logged-in user is a member of any of the authorized groups for the flow
                    if (request.getAuthorization().hasGroups()) {
                        for (val group : request.getAuthorization().getGroups()) {
                            if (groups.contains(UUID.fromString(group.getAuthenticationId()))) {
                                status = "200";
                            }
                        }
                    }
                }

                break;
            default:
                break;
        }

        $User user = new $User();
        user.setAuthenticationType(AuthorizationType.UsernamePassword);
        user.setDirectoryId("manywho-identity");
        user.setDirectoryName("ManyWho Identity");
        user.setUserId("");
        user.setStatus(status);

        return new ObjectDataResponse(typeBuilder.from(user));
    }

    public ObjectDataResponse groupAttributes() {
        List<AuthorizationAttribute> attributes = Lists.newArrayList();
        attributes.add(new AuthorizationAttribute("MEMBERS", "Members"));

        return new ObjectDataResponse(typeBuilder.from(attributes));
    }

    public ObjectDataResponse groups(ObjectDataRequest request) {
        ServiceConfiguration configuration = configurationParser.from(request);

        List<AuthorizationGroup> groups = Lists.newArrayList();

        if (request.getObjectData() == null || request.getObjectData().isEmpty()) {
            groups = repository.findAllGroups(configuration).stream()
                    .map(group -> new AuthorizationGroup(group.getId().toString(), group.getName(), group.getDescription()))
                    .collect(Collectors.toList());
        } else {
            val authorizationGroup = valueParser.asObject(request.getObjectData(), AuthorizationGroup.class);

            val group = repository.findGroup(configuration, authorizationGroup.getId());
            if (group != null) {
                groups.add(new AuthorizationGroup(group.getId().toString(), group.getName(), group.getDescription()));
            }
        }

        return new ObjectDataResponse(typeBuilder.from(groups));
    }

    public ObjectDataResponse userAttributes() {
        List<AuthorizationAttribute> attributes = Lists.newArrayList();
        attributes.add(new AuthorizationAttribute("MEMBERS", "Members"));

        return new ObjectDataResponse(typeBuilder.from(attributes));
    }

    public ObjectDataResponse users(ObjectDataRequest request) {
        ServiceConfiguration configuration = configurationParser.from(request);

        List<AuthorizationUser> users = Lists.newArrayList();

        if (request.getObjectData() == null || request.getObjectData().isEmpty()) {
            users = repository.findAllUsers(configuration).stream()
                    .map(user -> new AuthorizationUser(user.getId().toString(), user.getFullName(), user.getEmail()))
                    .collect(Collectors.toList());
        } else {
            val authorizationUser = valueParser.asObject(request.getObjectData(), AuthorizationUser.class);

            val user = repository.findUser(configuration, authorizationUser.getId());
            if (user != null) {
                users.add(new AuthorizationUser(user.getId().toString(), user.getFullName(), user.getEmail()));
            }
        }

        return new ObjectDataResponse(typeBuilder.from(users));
    }
}
