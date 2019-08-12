package com.extremum.watch.end2end.fixture;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.dao.impl.SpringDataMongoCommonDao;
import org.springframework.stereotype.Repository;

/**
 * @author rpuch
 */
@Repository
public interface WatchedModelDao extends MongoCommonDao<WatchedModel>, SpringDataMongoCommonDao<WatchedModel> {
}
