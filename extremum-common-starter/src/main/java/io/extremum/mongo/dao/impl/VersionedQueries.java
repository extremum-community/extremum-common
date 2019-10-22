package io.extremum.mongo.dao.impl;

import io.extremum.common.model.VersionedModel;
import io.extremum.mongo.SoftDeletion;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.ZonedDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class VersionedQueries {
    private final SoftDeletion softDeletion = new SoftDeletion();

    public Criteria actualSnapshot() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Criteria().andOperator(
                softDeletion.notDeleted(),
                where(VersionedModel.FIELDS.start.name()).lte(now),
                where(VersionedModel.FIELDS.end.name()).gt(now)
        );
    }
}
