package io.extremum.versioned.mongo.dao;

import org.springframework.data.mongodb.core.ReactiveMongoOperations;

public class TestReactiveMongoVersionedDaoImpl extends ReactiveMongoVersionedDaoImpl<TestMongoVersionedModel>
        implements TestReactiveMongoVersionedDao {
    public TestReactiveMongoVersionedDaoImpl(ReactiveMongoOperations mongoOperations) {
        super(mongoOperations);
    }
}
