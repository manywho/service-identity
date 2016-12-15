package com.manywho.services.identity.groups;

import com.google.inject.persist.Transactional;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.sdk.services.utils.UUIDs;
import com.manywho.services.identity.jpa.JpaQueryFactory;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupRepository {
    private final JpaQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final FilterHelper filterHelper;

    @Inject
    public GroupRepository(JpaQueryFactory queryFactory, EntityManager entityManager, FilterHelper filterHelper) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.filterHelper = filterHelper;
    }

    public List<Group> findAllByTenant(UUID tenant, ListFilter filter) {
        QGroupTable groupTable = QGroupTable.groupTable;

        JPAQuery<GroupTable> query = queryFactory.selectFrom(groupTable)
                .where(groupTable.tenantId.eq(tenant));

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(Group.class, filter.getOrderByPropertyDeveloperName());

            // Create the path on the entity, using the field name we just discovered
            StringPath path = new PathBuilder<>(Group.class, groupTable.getMetadata())
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

    @Transactional
    public void update(UUID tenant, Group group) {
        QGroupTable table = QGroupTable.groupTable;

        JPAUpdateClause updateClause = new JPAUpdateClause(entityManager, table);

        updateClause.set(table.name, group.getName())
                .set(table.description, group.getDescription())
                .where(table.id.eq(group.getId()))
                .where(table.tenantId.eq(tenant));

        updateClause.execute();
    }
}
