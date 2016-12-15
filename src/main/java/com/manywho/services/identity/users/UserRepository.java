package com.manywho.services.identity.users;

import com.google.inject.persist.Transactional;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.sdk.services.utils.UUIDs;
import com.manywho.services.identity.groups.Group;
import com.manywho.services.identity.groups.GroupTable;
import com.manywho.services.identity.jpa.JpaQueryFactory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserRepository {
    private final JpaQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final FilterHelper filterHelper;

    @Inject
    public UserRepository(JpaQueryFactory queryFactory, EntityManager entityManager, FilterHelper filterHelper) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.filterHelper = filterHelper;
    }

    @Transactional
    public void create(UUID tenant, User user) {
        UserTable userTable = new UserTable();
        userTable.setEmail(user.getEmail());
        userTable.setFirstName(user.getFirstName());
        userTable.setId(user.getId());
        userTable.setLastName(user.getLastName());
        userTable.setTenantId(tenant);

        for (Group group : user.getGroups()) {
            userTable.getGroups().add(entityManager.find(GroupTable.class, group.getId()));
        }

        entityManager.persist(userTable);
        entityManager.flush();
    }

    @Transactional
    public void delete(UUID tenant, User user) {
        QUserTable table = QUserTable.userTable;

        JPADeleteClause deleteClause = new JPADeleteClause(entityManager, table);

        deleteClause.where(table.tenantId.eq(tenant))
                .where(table.id.eq(user.getId()));

        deleteClause.execute();
    }

    public Boolean existsByEmail(UUID tenant, String email) {
        QUserTable table = QUserTable.userTable;

        JPAQuery<Boolean> query = queryFactory.select(
                queryFactory.select(Expressions.constant(1))
                        .from(table)
                        .where(table.tenantId.eq(tenant))
                        .where(table.email.eq(email))
                        .exists()
        ).from(table);

        return query.fetchOne();
    }

    public List<User> findAllByTenant(UUID tenant, ListFilter filter) {
        QUserTable userTable = QUserTable.userTable;

        JPAQuery<UserTable> query = queryFactory.selectFrom(userTable)
                .where(userTable.tenantId.eq(tenant));

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(User.class, filter.getOrderByPropertyDeveloperName());

            // Create the path on the entity, using the field name we just discovered
            StringPath path = new PathBuilder<>(UserTable.class, userTable.getMetadata())
                    .getString(fieldName);

            switch (filter.getOrderByDirectionType().toUpperCase()) {
                case "ASC":
                    query.orderBy(path.asc());
                    break;
                case "DESC":
                default:
                    query.orderBy(path.desc());
                    break;
            }
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

    public List<UUID> findGroups(UUID user) {
        QUserTable table = QUserTable.userTable;

        JPAQuery<UUID> query = queryFactory.select(table.groups.any().id)
                .from(table)
                .where(table.id.eq(user));

        return query.fetch();
    }

    @Transactional
    public void update(UUID tenant, User user) {
        QUserTable table = QUserTable.userTable;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, table);

        updateClause.set(table.firstName, user.getFirstName())
                .set(table.lastName, user.getLastName())
                .set(table.email, user.getEmail())
                .set(table.password, user.getPassword())
                .where(table.tenantId.eq(tenant))
                .where(table.id.eq(user.getId()));

        updateClause.execute();
    }
}
