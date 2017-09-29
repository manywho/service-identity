package com.manywho.services.identity.groups;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.sdk.services.utils.UUIDs;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.jpa.DslFactory;
import com.manywho.services.identity.jpa.Ordering;
import com.manywho.services.identity.users.QUserTable;
import com.manywho.services.identity.users.User;
import com.manywho.services.identity.users.UserTable;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupRepository {
    private final DslFactory dslFactory;
    private final FilterHelper filterHelper;

    @Inject
    public GroupRepository(DslFactory dslFactory, FilterHelper filterHelper) {
        this.dslFactory = dslFactory;
        this.filterHelper = filterHelper;
    }

    public void create(ServiceConfiguration configuration, Group group) {
        EntityManager entityManager = dslFactory.createEntityManager(configuration);

        GroupTable groupTable = new GroupTable();
        groupTable.setDescription(group.getDescription());
        groupTable.setId(group.getId());
        groupTable.setName(group.getName());

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        entityManager.persist(groupTable);
        entityManager.flush();

        transaction.commit();
    }

    public boolean existsByName(ServiceConfiguration configuration, String name) {
        SQLQueryFactory queryFactory = dslFactory.createSqlQueryFactory(configuration);

        QGroupTable table = new QGroupTable("Group");

        SQLQuery<Boolean> query = queryFactory.select(
                queryFactory.select(Expressions.constant(1))
                        .from(table)
                        .where(table.name.eq(name))
                        .groupBy(table.name)
                        .having(
                                table.id.count().gt(0)
                        )
                        .exists()
        );

        return query.fetchOne();
    }

    public Group find(ServiceConfiguration configuration, String id) {
        QGroupTable groupTable = QGroupTable.groupTable;

        JPAQuery<GroupTable> query = dslFactory.createJpaQueryFactory(configuration)
                .selectFrom(groupTable)
                .where(groupTable.id.eq(UUID.fromString(id)));

        return new Group(query.fetchOne());
    }

    public List<Group> findAllByTenant(ServiceConfiguration configuration, ListFilter filter) {
        QGroupTable groupTable = QGroupTable.groupTable;

        JPAQuery<GroupTable> query = dslFactory.createJpaQueryFactory(configuration).selectFrom(groupTable);

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(Group.class, filter.getOrderByPropertyDeveloperName());

            // Create the path on the entity, using the field name we just discovered
            StringPath path = new PathBuilder<>(Group.class, groupTable.getMetadata())
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
                query.where(groupTable.id.eq(UUID.fromString(filter.getSearch())));
            } else {
                // We want to search the fields as a fully surrounding wildcard (for now)
                String search = String.format("%%%s%%", filter.getSearch());

                // Search in the "name" and "description" fields
                query.where(
                        groupTable.name.likeIgnoreCase(search).or(
                                groupTable.description.likeIgnoreCase(search)
                        )
                );
            }
        }

        return query.fetch().stream()
                .map(Group::new)
                .collect(Collectors.toList());
    }

    public void update(ServiceConfiguration configuration, Group group) {
        EntityManager entityManager = dslFactory.createEntityManager(configuration);

        QGroupTable table = QGroupTable.groupTable;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, table)
                .set(table.name, group.getName())
                .set(table.description, group.getDescription())
                .where(table.id.eq(group.getId()));

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        updateClause.execute();

        transaction.commit();
    }
}
