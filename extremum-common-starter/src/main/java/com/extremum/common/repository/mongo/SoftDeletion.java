package com.extremum.common.repository.mongo;

import com.extremum.common.models.PersistableCommonModel;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
class SoftDeletion {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    Criteria notDeleted() {
        return new Criteria().orOperator(
                where(DELETED).exists(false),
                where(DELETED).is(false)
        );
    }
}
