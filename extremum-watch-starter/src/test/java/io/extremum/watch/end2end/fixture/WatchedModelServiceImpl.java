package io.extremum.watch.end2end.fixture;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class WatchedModelServiceImpl extends MongoCommonServiceImpl<WatchedModel> implements WatchedModelService {
    public WatchedModelServiceImpl(MongoCommonDao<WatchedModel> dao) {
        super(dao);
    }
}
