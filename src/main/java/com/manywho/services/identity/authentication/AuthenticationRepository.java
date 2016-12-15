package com.manywho.services.identity.authentication;

import com.manywho.services.identity.jpa.JpaQueryFactory;
import com.manywho.services.identity.users.QUserTable;
import com.manywho.services.identity.users.User;

import javax.inject.Inject;
import java.util.UUID;

public class AuthenticationRepository {
    private final JpaQueryFactory queryFactory;

    @Inject
    public AuthenticationRepository(JpaQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public User findUserByEmail(String email, UUID tenant) {
        QUserTable table = QUserTable.userTable;

        return new User(queryFactory.selectFrom(table)
                .where(table.email.eq(email))
                .where(table.tenantId.eq(tenant))
                .fetchOne());
    }
}
