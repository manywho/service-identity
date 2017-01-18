package com.manywho.services.identity.jpa;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public class Ordering {
    public static OrderSpecifier createOrderSpecifier(String orderDirection, ComparableExpressionBase path) {
        switch (orderDirection.toUpperCase()) {
            case "ASC":
                return path.asc();
            case "DESC":
            default:
                return path.desc();
        }
    }
}
