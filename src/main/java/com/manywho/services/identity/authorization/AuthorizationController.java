package com.manywho.services.identity.authorization;

import com.google.inject.Provider;
import com.manywho.sdk.api.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.api.run.elements.type.ObjectDataResponse;
import com.manywho.sdk.api.security.AuthenticatedWho;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/authorization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationController {
    private final Provider<AuthenticatedWho> authenticatedWhoProvider;
    private final AuthorizationManager manager;

    @Inject
    public AuthorizationController(Provider<AuthenticatedWho> authenticatedWhoProvider, AuthorizationManager manager) {
        this.authenticatedWhoProvider = authenticatedWhoProvider;
        this.manager = manager;
    }

    @Path("/")
    @POST
    public ObjectDataResponse authorize(ObjectDataRequest request) {
        return manager.authorize(authenticatedWhoProvider.get(), request);
    }

    @Path("/group/attribute")
    @POST
    public ObjectDataResponse groupAttributes(ObjectDataRequest request) {
        return manager.groupAttributes(authenticatedWhoProvider.get(), request);
    }

    @Path("/group")
    @POST
    public ObjectDataResponse groups(ObjectDataRequest request) {
        return manager.groups(authenticatedWhoProvider.get(), request);
    }

    @Path("/user/attribute")
    @POST
    public ObjectDataResponse userAttributes(ObjectDataRequest request) {
        return manager.userAttributes(authenticatedWhoProvider.get(), request);
    }

    @Path("/user")
    @POST
    public ObjectDataResponse users(ObjectDataRequest request) {
        return manager.users(authenticatedWhoProvider.get(), request);
    }
}
