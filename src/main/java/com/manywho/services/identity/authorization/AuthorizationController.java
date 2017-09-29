package com.manywho.services.identity.authorization;

import com.google.inject.Provider;
import com.manywho.sdk.api.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.api.run.elements.type.ObjectDataResponse;
import com.manywho.sdk.api.security.AuthenticatedWho;
import com.manywho.sdk.services.controllers.AbstractAuthorizationController;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/authorization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationController extends AbstractAuthorizationController {
    private final Provider<AuthenticatedWho> authenticatedWhoProvider;
    private final AuthorizationManager manager;

    @Inject
    public AuthorizationController(Provider<AuthenticatedWho> authenticatedWhoProvider, AuthorizationManager manager) {
        this.authenticatedWhoProvider = authenticatedWhoProvider;
        this.manager = manager;
    }

    @Path("/")
    @POST
    public ObjectDataResponse authorization(ObjectDataRequest request) {
        return manager.authorize(authenticatedWhoProvider.get(), request);
    }

    @Path("/group/attribute")
    @POST
    public ObjectDataResponse groupAttributes() {
        return manager.groupAttributes();
    }

    @Path("/group")
    @POST
    public ObjectDataResponse groups(ObjectDataRequest request) {
        return manager.groups(request);
    }

    @Path("/user/attribute")
    @POST
    public ObjectDataResponse userAttributes() {
        return manager.userAttributes();
    }

    @Path("/user")
    @POST
    public ObjectDataResponse users(ObjectDataRequest request) {
        return manager.users(request);
    }
}
