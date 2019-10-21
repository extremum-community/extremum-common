package io.extremum.versioned.mongo.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ReactiveVersionedMongoDaoTestConfiguration.class)
class ReactiveMongoVersionedDaoTest extends TestWithServices {
    @Autowired
    private TestReactiveMongoVersionedDao dao;

    @Test
    void test() {
        TestMongoVersionedModel model = new TestMongoVersionedModel();
        model.setName("New model");

        TestMongoVersionedModel savedModel = dao.save(model).block();

        TestMongoVersionedModel retrievedModel = dao.findById(savedModel.getHistoryId()).block();
        System.out.println(retrievedModel);
    }

    @Test
    void test2() {
        TestMongoVersionedModel model = new TestMongoVersionedModel();
        model.setName("New model");

        TestMongoVersionedModel savedModel = dao.save(model).block();
        savedModel.setName("Another name");
        savedModel = dao.save(savedModel).block();

        TestMongoVersionedModel retrievedModel = dao.findById(savedModel.getHistoryId()).block();
        System.out.println(retrievedModel);
    }
}
