package com.manywho.services.identity.jpa;

import com.manywho.sdk.api.run.elements.type.ListFilter;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public class Ordering {
    public static OrderSpecifier createOrderSpecifier(ListFilter.OrderByDirectionType orderDirection, ComparableExpressionBase path) {
        switch (orderDirection) {
            case Ascending:
                return path.asc();
            case Descending:
            default:
                return path.desc();
        }
    }
}
