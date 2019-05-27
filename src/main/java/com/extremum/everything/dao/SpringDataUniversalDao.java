package com.extremum.everything.dao;

import com.extremum.common.models.PersistableCommonModel;
import com.extremum.everything.collection.Projection;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
@Repository
public class SpringDataUniversalDao implements UniversalDao {
    private static final String CREATED = PersistableCommonModel.FIELDS.created.name();

    private final MongoOperations mongoOperations;

    public SpringDataUniversalDao(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public <T> List<T> retrieveByIds(List<?> ids, Class<T> classOfElement, Projection projection) {
        List<Criteria> criteria = new ArrayList<>();

        criteria.add(where(PersistableCommonModel.FIELDS.id.name()).in(ids));
        criteria.add(
                new Criteria().orOperator(
                        where(PersistableCommonModel.FIELDS.deleted.name()).exists(false),
                        where(PersistableCommonModel.FIELDS.deleted.name()).is(false)
                )
        );

        projection.getSince().ifPresent(since -> criteria.add(where(CREATED).gte(since)));
        projection.getUntil().ifPresent(until -> criteria.add(where(CREATED).lte(until)));

        Query query = new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        projection.getLimit().ifPresent(query::limit);
        projection.getOffset().ifPresent(query::skip);

        query.with(Sort.by(
                Order.by(CREATED),
                Order.by(PersistableCommonModel.FIELDS.id.name())
        ));

        return mongoOperations.find(query, classOfElement);
    }
}
