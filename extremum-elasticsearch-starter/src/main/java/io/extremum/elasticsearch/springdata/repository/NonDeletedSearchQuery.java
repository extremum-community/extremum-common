package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.SoftDeletion;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

/**
 * @author rpuch
 */
class NonDeletedSearchQuery extends DelegateSearchQuery {
    private final SearchQuery query;

    NonDeletedSearchQuery(SearchQuery query) {
        super(query);
        this.query = query;
    }

    @Override
    public QueryBuilder getQuery() {
        return new SoftDeletion().amendQueryBuilderWithNotDeletedCondition(query.getQuery());
    }
}
