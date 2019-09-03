package io.extremum.watch.end2end.fixture;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.dao.impl.SpringDataMongoCommonDao;
import org.springframework.stereotype.Repository;

/**
 * @author rpuch
 */
@Repository
public interface WatchedModelDao extends MongoCommonDao<WatchedModel>, SpringDataMongoCommonDao<WatchedModel> {
}
