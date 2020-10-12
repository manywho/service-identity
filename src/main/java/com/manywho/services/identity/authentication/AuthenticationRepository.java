package com.manywho.services.identity.authentication;

import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.jdbi.JdbiFactory;
import com.manywho.services.identity.users.User;
import lombok.val;

import javax.inject.Inject;

public class AuthenticationRepository {
    private final JdbiFactory jdbiFactory;

    @Inject
    public AuthenticationRepository(JdbiFactory jdbiFactory) {
        this.jdbiFactory = jdbiFactory;
    }

    public User findUserByEmail(ServiceConfiguration configuration, String email) {
        final String sql = "SELECT id, first_name, last_name, email, password, created_at, updated_at FROM \"User\" WHERE email = :email";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                    .bind("email", email)
                    .mapToBean(User.class)
                    .findFirst()
                    .orElse(null));
    }
}
