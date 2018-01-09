package com.manywho.services.identity.users;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgOffsetClause;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.jdbi.JdbiFactory;
import com.manywho.services.identity.jdbi.Ordering;
import com.manywho.services.identity.utils.UUIDs;
import lombok.val;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.*;

public class UserRepository {
    private final JdbiFactory jdbiFactory;
    private final FilterHelper filterHelper;

    @Inject
    public UserRepository(JdbiFactory jdbiFactory, FilterHelper filterHelper) {
        this.jdbiFactory = jdbiFactory;
        this.filterHelper = filterHelper;
    }

    public void create(ServiceConfiguration configuration, User user) {
        final String sql = "INSERT INTO \"User\" (id, email, first_name, last_name, password, updated_at) VALUES (:id, :email, :firstName, :lastName, :password, :updatedAt)";

        try (val handle = jdbiFactory.create(configuration).open()) {
            handle.useTransaction(transaction -> {
                // First insert the user
                transaction.createUpdate(sql)
                        .bind("id", user.getId())
                        .bind("email", user.getEmail())
                        .bind("firstName", user.getFirstName())
                        .bind("lastName", user.getLastName())
                        .bind("password", user.getPassword())
                        .bind("updatedAt", OffsetDateTime.now())
                        .execute();

                // Then insert all the groups the user is a member of
                for (Group group : user.getGroups()) {
                    transaction.createUpdate("INSERT INTO \"Membership\" (user_id, group_id) VALUES (:user, :group)")
                            .bind("user", user.getId())
                            .bind("group", group.getId())
                            .execute();
                }
            });
        }
    }

    public void delete(ServiceConfiguration configuration, User user) {
        final String sql = "DELETE FROM \"User\" WHERE id = :id";

        jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createUpdate(sql)
                        .bind("id", user.getId())
                        .execute());
    }

    public Boolean existsByEmail(ServiceConfiguration configuration, String email) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM \"User\" WHERE email = :email)";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                        .bind("email", email)
                        .mapTo(boolean.class)
                        .findOnly());
    }

    public User find(ServiceConfiguration configuration, String id) {
        final String sql = "SELECT id, first_name, last_name, email, created_at, updated_at FROM \"User\" WHERE id = :id";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                        .bind("id", UUID.fromString(id))
                        .mapToBean(User.class)
                        .findOnly());
    }

    public Collection<User> findAllByTenant(ServiceConfiguration configuration, ListFilter filter) {
        SelectQuery selectQuery = new SelectQuery()
                .addAliasedColumn(new CustomSql("u.id"), "u_id")
                .addAliasedColumn(new CustomSql("u.first_name"), "u_first_name")
                .addAliasedColumn(new CustomSql("u.last_name"), "u_last_name")
                .addAliasedColumn(new CustomSql("u.email"), "u_email")
                .addAliasedColumn(new CustomSql("u.created_at"), "u_created_at")
                .addAliasedColumn(new CustomSql("u.updated_at"), "u_updated_at")
                .addAliasedColumn(new CustomSql("g.id"), "g_id")
                .addAliasedColumn(new CustomSql("g.name"), "g_name")
                .addAliasedColumn(new CustomSql("g.description"), "g_description")
                .addCustomFromTable("\"User\" u")
                .addCustomJoin(
                        SelectQuery.JoinType.LEFT_OUTER,
                        "u",
                        "\"Membership\" m",
                        BinaryCondition.equalTo(new CustomSql("u.id"), new CustomSql("m.user_id"))
                )
                .addCustomJoin(
                        SelectQuery.JoinType.LEFT_OUTER,
                        "m",
                        "\"Group\" g",
                        BinaryCondition.equalTo(new CustomSql("g.id"), new CustomSql("m.group_id"))
                );

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(User.class, filter.getOrderByPropertyDeveloperName());

            // Order by the given direction in the ListFilter
            selectQuery.addCustomOrdering(fieldName, Ordering.createOrderSpecifier(filter.getOrderByDirectionType()));
        }

        if (filter.hasLimit()) {
            selectQuery.addCustomization(new PgLimitClause(filter.getLimit()));
        }

        if (filter.hasOffset()) {
            selectQuery.addCustomization(new PgOffsetClause(filter.getOffset()));
        }

        Map<String, Object> bindings = new HashMap<>();

        if (filter.hasSearch()) {
            // If the search query was a valid UUID then we'll try and filter by ID
            if (UUIDs.isValid(filter.getSearch())) {
                selectQuery.addCondition(new CustomCondition("id = :id"));

                bindings.put("id", UUID.fromString(filter.getSearch()));
            } else {
                // Search in the "firstName", "lastName" and "email" fields
                selectQuery.addCondition(new CustomCondition("first_name ILIKE '%' || :search || '%' OR last_name ILIKE '%' || :search || '%' OR email ILIKE '%' || :search || '%'"));

                bindings.put("search", filter.getSearch());
            }
        }

        try (val handle = jdbiFactory.create(configuration).open()) {
            val query = handle.createQuery(selectQuery.toString());

            for (val binding : bindings.entrySet()) {
                query.bind(binding.getKey(), binding.getValue());
            }

            return query.registerRowMapper(BeanMapper.factory(User.class, "u"))
                    .registerRowMapper(BeanMapper.factory(Group.class, "g"))
                    .reduceRows(new LinkedHashMap<UUID, User>(), (accumulator, row) -> {
                        User user = accumulator.computeIfAbsent(
                                row.getColumn("u_id", UUID.class),
                                id -> row.getRow(User.class)
                        );

                        if (row.getColumn("g_id", UUID.class) != null) {
                            user.getGroups().add(row.getRow(Group.class));
                        }

                        return accumulator;
                    })
                    .values();
        }
    }

    public List<UUID> findGroups(ServiceConfiguration configuration, UUID user) {
        final String sql = "SELECT group_id FROM \"Membership\" WHERE user_id = :user";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                        .bind("user", user)
                        .mapTo(UUID.class)
                        .list());
    }

    public void update(ServiceConfiguration configuration, User user) {
        final String sql = "UPDATE \"User\" SET first_name = :firstName, last_name = :lastName, email = :email, updated_at = :updatedAt WHERE id = :id";

        try (val handle = jdbiFactory.create(configuration).open()) {
            handle.useTransaction(transaction -> {
                transaction.createUpdate(sql)
                        .bind("firstName", user.getFirstName())
                        .bind("lastName", user.getLastName())
                        .bind("email", user.getEmail())
                        .bind("id", user.getId())
                        .bind("updatedAt", OffsetDateTime.now())
                        .execute();

                // If we're given a password to update, then update it
                if (user.getPassword() != null) {
                    transaction.createUpdate("UPDATE \"User\" SET password = :password WHERE id = :id")
                            .bind("id", user.getId())
                            .execute();
                }

                // If the user has some groups attached, we want to add them as members
                transaction.createUpdate("DELETE FROM \"Membership\" WHERE user_id = :user")
                        .bind("user", user.getId())
                        .execute();

                // After deleting all their memberships, we add the new memberships
                for (val group : user.getGroups()) {
                    transaction.createUpdate("INSERT INTO \"Membership\" (group_id, user_id) VALUES (:group, :user)")
                            .bind("group", group.getId())
                            .bind("user", user.getId())
                            .execute();
                }
            });
        }
    }
}
