package com.manywho.services.identity.health;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/health")
public class HealthController {

    @GET
    public Response health() {
        return Response.ok()
                .build();
    }
}
