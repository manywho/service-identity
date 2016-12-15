package com.manywho.services.identity.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class JpaQueryFactory extends JPAQueryFactory {
    @Inject
    public JpaQueryFactory(EntityManager entityManager) {
        super(entityManager);
    }
}
