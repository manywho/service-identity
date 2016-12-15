package com.manywho.services.identity.jpa;

import com.google.inject.persist.PersistService;

import javax.inject.Inject;

public class JpaInitializer {
    @Inject
    public JpaInitializer(PersistService persistService) {
        persistService.start();
    }
}
