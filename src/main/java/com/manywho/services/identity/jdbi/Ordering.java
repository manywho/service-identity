package com.manywho.services.identity.jdbi;

import com.healthmarketscience.sqlbuilder.OrderObject;
import com.manywho.sdk.api.run.elements.type.ListFilter;

public class Ordering {
    public static OrderObject.Dir createOrderSpecifier(ListFilter.OrderByDirectionType orderDirection) {
        switch (orderDirection) {
            case Ascending:
                return OrderObject.Dir.ASCENDING;
            case Descending:
            default:
                return OrderObject.Dir.DESCENDING;
        }
    }
}
