package com.manywho.services.identity.users;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.sdk.services.utils.UUIDs;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.groups.GroupTable;
import com.manywho.services.identity.jpa.JpaFactory;
import com.manywho.services.identity.jpa.Ordering;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserRepository {
    private final JpaFactory jpaFactory;
    private final FilterHelper filterHelper;

    @Inject
    public UserRepository(JpaFactory jpaFactory, FilterHelper filterHelper) {
        this.jpaFactory = jpaFactory;
        this.filterHelper = filterHelper;
    }

    public void create(ServiceConfiguration configuration, User user) {
        EntityManager entityManager = jpaFactory.createEntityManager(configuration);

        UserTable userTable = new UserTable();
        userTable.setEmail(user.getEmail());
        userTable.setFirstName(user.getFirstName());
        userTable.setId(user.getId());
        userTable.setLastName(user.getLastName());

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
        EntityManager entityManager = jpaFactory.createEntityManager(configuration);

        QUserTable table = QUserTable.userTable;

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        new JPADeleteClause(entityManager, table)
                .where(table.id.eq(user.getId()))
                .execute();

        transaction.commit();
    }

    public Boolean existsByEmail(ServiceConfiguration configuration, String email) {
        JPAQueryFactory queryFactory = jpaFactory.createQueryFactory(configuration);

        QUserTable table = QUserTable.userTable;

        JPAQuery<Boolean> query = queryFactory.select(
                queryFactory.select(Expressions.constant(1))
                        .from(table)
                        .where(table.email.eq(email))
                        .exists()
        ).from(table);

        return query.fetchOne();
    }

    public List<User> findAllByTenant(ServiceConfiguration configuration, ListFilter filter) {
        QUserTable userTable = QUserTable.userTable;

        JPAQuery<UserTable> query = jpaFactory.createQueryFactory(configuration).selectFrom(userTable);

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
        QUserTable table = QUserTable.userTable;

        JPAQuery<UUID> query = jpaFactory.createQueryFactory(configuration).select(table.groups.any().id)
                .from(table)
                .where(table.id.eq(user));

        return query.fetch();
    }

    public void update(ServiceConfiguration configuration, User user) {
        EntityManager entityManager = jpaFactory.createEntityManager(configuration);

        QUserTable table = QUserTable.userTable;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, table)
                .set(table.firstName, user.getFirstName())
                .set(table.lastName, user.getLastName())
                .set(table.email, user.getEmail())
                .set(table.password, user.getPassword())
                .where(table.id.eq(user.getId()));

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        updateClause.execute();

        transaction.commit();
    }
}
