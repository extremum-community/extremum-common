package common.dao.mongo;

import io.extremum.common.test.TestWithServices;
import models.TestMongoModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class ReactiveMongoCommonDaoModelLifecycleTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveMongoModelDao dao;

    @Test
    void whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoModel model = new TestMongoModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestMongoModel createdModel = dao.save(model).block();
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getUuid());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void whenFinding_thenDescriptorShouldBeFilled() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();

        TestMongoModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel.getUuid(), is(notNullValue()));
    }

    @Test
    void whenFinding_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoModel savedModel = dao.save(new TestMongoModel()).block();

        TestMongoModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel.getUuid().getInternalId(), is(equalTo(savedModel.getId().toString())));
    }
}
