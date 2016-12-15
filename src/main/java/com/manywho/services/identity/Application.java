package com.manywho.services.identity;

import com.manywho.sdk.services.servers.EmbeddedServer;
import com.manywho.sdk.services.servers.undertow.UndertowServer;
import com.manywho.services.identity.jpa.JpaModule;

public class Application {
    public static void main(String[] args) throws Exception {
        EmbeddedServer server = new UndertowServer();
        server.addModule(new ApplicationModule());
        server.addModule(new JpaModule());
        server.setApplication(Application.class);
        server.start();
    }
}
