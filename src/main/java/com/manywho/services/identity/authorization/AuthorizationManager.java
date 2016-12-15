package com.manywho.services.identity.authorization;

import com.google.common.collect.Lists;
import com.manywho.sdk.api.AuthorizationType;
import com.manywho.sdk.api.run.elements.config.Group;
import com.manywho.sdk.api.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.api.run.elements.type.ObjectDataResponse;
import com.manywho.sdk.api.security.AuthenticatedWho;
import com.manywho.sdk.services.types.TypeBuilder;
import com.manywho.sdk.services.types.system.$User;
import com.manywho.sdk.services.types.system.AuthorizationAttribute;
import com.manywho.sdk.services.types.system.AuthorizationGroup;
import com.manywho.sdk.services.types.system.AuthorizationUser;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthorizationManager {
    private final TypeBuilder typeBuilder;
    private final AuthorizationRepository repository;

    @Inject
    public AuthorizationManager(TypeBuilder typeBuilder, AuthorizationRepository repository) {
        this.typeBuilder = typeBuilder;
        this.repository = repository;
    }

    public ObjectDataResponse authorize(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        String status = "401";

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
                    List<UUID> groups = repository.findGroupsForUser(UUID.fromString(authenticatedWho.getUserId()));

                    // Check if the logged-in user is allowed to authorize against the flow
                    if (request.getAuthorization().hasUsers()) {
                        if (request.getAuthorization().getUsers().stream()
                                .anyMatch(user -> authenticatedWho.getUserId().equals(user.getAuthenticationId()))) {
                            status = "200";
                        }
                    }

                    // Check if the logged-in user is a member of any of the authorized groups for the flow
                    if (request.getAuthorization().hasGroups()) {
                        for (Group group : request.getAuthorization().getGroups()) {
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

    public ObjectDataResponse groupAttributes(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        List<AuthorizationAttribute> attributes = Lists.newArrayList();
        attributes.add(new AuthorizationAttribute("MEMBERS", "Members"));

        return new ObjectDataResponse(typeBuilder.from(attributes));
    }

    public ObjectDataResponse groups(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        List<AuthorizationGroup> groups = repository.findAllGroups(authenticatedWho.getManyWhoTenantId()).stream()
                .map(group -> new AuthorizationGroup(group.getId().toString(), group.getName(), group.getDescription()))
                .collect(Collectors.toList());

        return new ObjectDataResponse(typeBuilder.from(groups));
    }

    public ObjectDataResponse userAttributes(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        List<AuthorizationAttribute> attributes = Lists.newArrayList();
        attributes.add(new AuthorizationAttribute("MEMBERS", "Members"));

        return new ObjectDataResponse(typeBuilder.from(attributes));
    }

    public ObjectDataResponse users(AuthenticatedWho authenticatedWho, ObjectDataRequest request) {
        List<AuthorizationUser> users = repository.findAllUsers(authenticatedWho.getManyWhoTenantId()).stream()
                .map(user -> new AuthorizationUser(user.getId().toString(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());

        return new ObjectDataResponse(typeBuilder.from(users));
    }
}
