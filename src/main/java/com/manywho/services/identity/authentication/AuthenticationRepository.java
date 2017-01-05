package com.manywho.services.identity.authentication;

import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.jpa.JpaFactory;
import com.manywho.services.identity.users.QUserTable;
import com.manywho.services.identity.users.User;
import com.manywho.services.identity.users.UserTable;

import javax.inject.Inject;

public class AuthenticationRepository {
    private final JpaFactory jpaFactory;

    @Inject
    public AuthenticationRepository(JpaFactory jpaFactory) {
        this.jpaFactory = jpaFactory;
    }

    public User findUserByEmail(ServiceConfiguration configuration, String email) {
        QUserTable table = QUserTable.userTable;

        UserTable user = jpaFactory.createQueryFactory(configuration).selectFrom(table)
                .where(table.email.eq(email))
                .fetchOne();

        if (user == null) {
            throw new RuntimeException("Unable to find a user with those credentials");
        }

        return new User(user);
    }
}
