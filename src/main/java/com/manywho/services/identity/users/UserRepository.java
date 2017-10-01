package com.manywho.services.identity.users;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.sdk.services.utils.UUIDs;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.groups.GroupTable;
import com.manywho.services.identity.jpa.DslFactory;
import com.manywho.services.identity.jpa.Ordering;
import com.manywho.services.identity.memberships.MembershipTable;
import com.manywho.services.identity.memberships.QMembershipTable;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.experimental.var;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserRepository {
    private final DslFactory dslFactory;
    private final FilterHelper filterHelper;

    @Inject
    public UserRepository(DslFactory dslFactory, FilterHelper filterHelper) {
        this.dslFactory = dslFactory;
        this.filterHelper = filterHelper;
    }

    public void create(ServiceConfiguration configuration, User user) {
        EntityManager entityManager = dslFactory.createEntityManager(configuration);

        UserTable userTable = new UserTable();
        userTable.setEmail(user.getEmail());
        userTable.setFirstName(user.getFirstName());
        userTable.setId(user.getId());
        userTable.setLastName(user.getLastName());
        userTable.setPassword(user.getPassword());

        for (Group group : user.getGroups()) {
            userTable.getGroups().add(entityManager.find(GroupTable.class, group.getId()));
        }

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.persist(userTable);
        entityManager.flush();

        transaction.commit();
    }

    public void delete(ServiceConfiguration configuration, User user) {
        EntityManager entityManager = dslFactory.createEntityManager(configuration);

        QUserTable table = QUserTable.userTable;

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        new JPADeleteClause(entityManager, table)
                .where(table.id.eq(user.getId()))
                .execute();

        transaction.commit();
    }

    public Boolean existsByEmail(ServiceConfiguration configuration, String email) {
        SQLQueryFactory queryFactory = dslFactory.createSqlQueryFactory(configuration);

        QUserTable table = new QUserTable("User");

        SQLQuery<Boolean> query = queryFactory.select(
                queryFactory.select(Expressions.constant(1))
                        .from(table)
                        .where(table.email.eq(email))
                        .exists()
        );

        return query.fetchOne();
    }

    public User find(ServiceConfiguration configuration, String id) {
        QUserTable userTable = QUserTable.userTable;

        JPAQuery<UserTable> query = dslFactory.createJpaQueryFactory(configuration)
                .selectFrom(userTable)
                .where(userTable.id.eq(UUID.fromString(id)));

        return new User(query.fetchOne());
    }

    public List<User> findAllByTenant(ServiceConfiguration configuration, ListFilter filter) {
        QUserTable userTable = QUserTable.userTable;

        JPAQuery<UserTable> query = dslFactory.createJpaQueryFactory(configuration).selectFrom(userTable);

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(User.class, filter.getOrderByPropertyDeveloperName());

            // Create the path on the entity, using the field name we just discovered
            StringPath path = new PathBuilder<>(UserTable.class, userTable.getMetadata())
                    .getString(fieldName);

            // Order by the given direction in the ListFilter
            query.orderBy(Ordering.createOrderSpecifier(filter.getOrderByDirectionType(), path));
        }

        if (filter.hasLimit()) {
            query.limit(filter.getLimit());
        }

        if (filter.hasOffset()) {
            query.offset(filter.getOffset());
        }

        if (filter.hasSearch()) {
            // If the search query was a valid UUID then we'll try and filter by ID
            if (UUIDs.isValid(filter.getSearch())) {
                query.where(userTable.id.eq(UUID.fromString(filter.getSearch())));
            } else {
                // We want to search the fields as a fully surrounding wildcard (for now)
                String search = String.format("%%%s%%", filter.getSearch());

                // Search in the "firstName", "lastName" and "email" fields
                query.where(
                        userTable.firstName.likeIgnoreCase(search).or(
                                userTable.lastName.likeIgnoreCase(search).or(
                                        userTable.email.likeIgnoreCase(search)
                                )
                        )
                );
            }
        }

        return query.fetch().stream()
                .map(User::new)
                .collect(Collectors.toList());
    }

    public List<UUID> findGroups(ServiceConfiguration configuration, UUID user) {
        QMembershipTable table = QMembershipTable.membershipTable;

        JPAQuery<UUID> query = dslFactory.createJpaQueryFactory(configuration).select(table.group)
                .from(table)
                .where(table.user.eq(user));

        return query.fetch();
    }

    public void update(ServiceConfiguration configuration, User user) {
        EntityManager entityManager = dslFactory.createEntityManager(configuration);

        QUserTable table = QUserTable.userTable;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, table)
                .set(table.firstName, user.getFirstName())
                .set(table.lastName, user.getLastName())
                .set(table.email, user.getEmail())
                .where(table.id.eq(user.getId()));

        if (user.getPassword() != null) {
            updateClause.set(table.password, user.getPassword());
        }

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        updateClause.execute();

        // If the user has some groups attached, we want to add them as members
        QMembershipTable membershipTable = QMembershipTable.membershipTable;

        // First delete all their memberships
        new JPADeleteClause(entityManager, membershipTable)
                .where(membershipTable.user.eq(user.getId()))
                .execute();

        // Now add the new memberships
        for (var group : user.getGroups()) {
            entityManager.persist(new MembershipTable(user.getId(), group.getId()));
        }

        entityManager.flush();

        transaction.commit();
    }
}
