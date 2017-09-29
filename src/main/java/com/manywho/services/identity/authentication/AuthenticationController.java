package com.manywho.services.identity.authentication;

import com.manywho.sdk.api.security.AuthenticatedWhoResult;
import com.manywho.sdk.api.security.AuthenticationCredentials;
import com.manywho.sdk.services.controllers.AbstractAuthenticationController;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/authentication")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationController extends AbstractAuthenticationController {
    private final AuthenticationManager manager;

    @Inject
    public AuthenticationController(AuthenticationManager manager) {
        this.manager = manager;
    }

    @Path("/")
    @POST
    public AuthenticatedWhoResult authentication(AuthenticationCredentials credentials) {
        return manager.authenticate(credentials);
    }
}
