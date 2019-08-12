package com.extremum.watch.end2end.fixture;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
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
