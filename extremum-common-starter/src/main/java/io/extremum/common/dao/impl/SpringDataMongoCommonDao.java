package io.extremum.common.dao.impl;

import io.extremum.common.dao.MongoCommonDao;
import io.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataMongoCommonDao<M extends MongoCommonModel>
        extends MongoCommonDao<M>, MongoRepository<M, ObjectId> {
}
