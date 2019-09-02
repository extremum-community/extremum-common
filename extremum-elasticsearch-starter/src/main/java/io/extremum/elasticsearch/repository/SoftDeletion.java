package io.extremum.elasticsearch.repository;

import io.extremum.common.model.PersistableCommonModel;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.query.Criteria;

import static org.springframework.data.elasticsearch.core.query.Criteria.where;

/**
 * @author rpuch
 */
class SoftDeletion {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    Criteria notDeleted() {
        return where(DELETED).not().is(true);
    }

    QueryBuilder amendQueryBuilderWithNotDeletedCondition(QueryBuilder query) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(query);
        QueryBuilder notDeletedQuery = new CriteriaQueryProcessor().createQueryFromCriteria(notDeleted());
        boolQueryBuilder.must(notDeletedQuery);

        return boolQueryBuilder;
    }
}
