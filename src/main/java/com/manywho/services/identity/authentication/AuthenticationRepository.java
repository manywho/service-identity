package com.manywho.services.identity.authentication;

import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.jpa.DslFactory;
import com.manywho.services.identity.users.QUserTable;
import com.manywho.services.identity.users.User;
import com.manywho.services.identity.users.UserTable;

import javax.inject.Inject;

public class AuthenticationRepository {
    private final DslFactory dslFactory;

    @Inject
    public AuthenticationRepository(DslFactory dslFactory) {
        this.dslFactory = dslFactory;
    }

    public User findUserByEmail(ServiceConfiguration configuration, String email) {
        QUserTable table = QUserTable.userTable;

        UserTable user = dslFactory.createJpaQueryFactory(configuration).selectFrom(table)
                .where(table.email.eq(email))
                .fetchOne();

        if (user == null) {
            throw new RuntimeException("Unable to find a user with those credentials");
        }

        return new User(user);
    }
}
