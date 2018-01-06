package com.manywho.services.identity.groups;

import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgOffsetClause;
import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.manywho.sdk.services.database.FilterHelper;
import com.manywho.services.identity.ServiceConfiguration;
import com.manywho.services.identity.jdbi.JdbiFactory;
import com.manywho.services.identity.jdbi.Ordering;
import com.manywho.services.identity.utils.UUIDs;
import lombok.val;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupRepository {
    private final JdbiFactory jdbiFactory;
    private final FilterHelper filterHelper;

    @Inject
    public GroupRepository(JdbiFactory jdbiFactory, FilterHelper filterHelper) {
        this.jdbiFactory = jdbiFactory;
        this.filterHelper = filterHelper;
    }

    public void create(ServiceConfiguration configuration, Group group) {
        final String sql = "INSERT INTO \"Group\" (id, name, description, updated_at) VALUES (:id, :name, :description, :updatedAt)";

        jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createUpdate(sql)
                        .bind("id", group.getId())
                        .bind("name", group.getName())
                        .bind("description", group.getDescription())
                        .bind("updatedAt", OffsetDateTime.now())
                        .execute());
    }

    public boolean existsByName(ServiceConfiguration configuration, String name) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM \"Group\" WHERE name = :name GROUP BY name HAVING COUNT(id) > 0)";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                        .bind("name", name)
                        .mapTo(boolean.class)
                        .findOnly());
    }

    public Group find(ServiceConfiguration configuration, String id) {
        final String sql = "SELECT id, name, description, created_at, updated_at FROM \"Group\" WHERE id = :id";

        return jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createQuery(sql)
                        .bind("id", UUID.fromString(id))
                        .mapToBean(Group.class)
                        .findOnly());
    }

    public List<Group> findAllByTenant(ServiceConfiguration configuration, ListFilter filter) {
        SelectQuery selectQuery = new SelectQuery()
                .addAllColumns()
                .addCustomFromTable("\"Group\"");

        // If we're given a property to order by, the we'll find it in the type and order the query by it
        if (filter.hasOrderByPropertyDeveloperName()) {
            String fieldName = filterHelper.findFieldName(Group.class, filter.getOrderByPropertyDeveloperName());

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
                // Search in the "name" and "description" fields
                selectQuery.addCondition(new CustomCondition("name ILIKE '%' || :search || '%' OR description ILIKE '%' || :search || '%'"));

                bindings.put("search", filter.getSearch());
            }
        }

        try (val handle = jdbiFactory.create(configuration).open()) {
            val query = handle.createQuery(selectQuery.toString());

            for (val binding : bindings.entrySet()) {
                query.bind(binding.getKey(), binding.getValue());
            }

            return query.mapToBean(Group.class)
                    .list();
        }
    }

    public void update(ServiceConfiguration configuration, Group group) {
        final String sql = "UPDATE \"Group\" SET name = :name, description = :description, updated_at = :updatedAt WHERE id = :id";

        jdbiFactory.create(configuration)
                .withHandle(handle -> handle.createUpdate(sql)
                        .bind("name", group.getName())
                        .bind("description", group.getDescription())
                        .bind("id", group.getId())
                        .bind("updatedAt", OffsetDateTime.now())
                        .execute());
    }
}
