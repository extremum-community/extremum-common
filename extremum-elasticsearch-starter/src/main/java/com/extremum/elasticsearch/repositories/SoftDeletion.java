package com.extremum.elasticsearch.repositories;

import com.extremum.common.models.PersistableCommonModel;
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
}
