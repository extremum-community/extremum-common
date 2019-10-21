package io.extremum.versioned.mongo.dao;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ReactiveVersionedMongoDaoTestConfiguration.class)
class ReactiveMongoCommonDaoModelLifecycleTest extends TestWithServices {
    @Autowired
    private TestReactiveMongoVersionedDao dao;

    @Test
    void freshModelShouldNotHaveSystemFieldsFilled() {
        TestMongoVersionedModel model = new TestMongoVersionedModel();

        assertNull(model.getUuid());
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());
        assertNull(model.getStart());
        assertNull(model.getEnd());
    }

    private void assertThatSystemFieldsAreFilled(TestMongoVersionedModel createdModel) {
        assertNotNull(createdModel.getId());
        assertNotNull(createdModel.getUuid());
        assertNotNull(createdModel.getCreated());
        assertNotNull(createdModel.getModified());
        assertNotNull(createdModel.getStart());
        assertNotNull(createdModel.getEnd());
        assertNotNull(createdModel.getVersion());
        assertFalse(createdModel.getDeleted());
    }

    @Test
    void givenEntityIsNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoVersionedModel savedModel = dao.save(new TestMongoVersionedModel()).block();

        assertThatSystemFieldsAreFilled(savedModel);
    }

    @Test
    void givenEntityIsNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        List<TestMongoVersionedModel> savedModels = dao.saveAll(singletonList(new TestMongoVersionedModel()))
                .toStream().collect(Collectors.toList());

        assertThat(savedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(savedModels.get(0));
    }

    @Test
    void givenEntityIsNotNew_whenSaving_thenAllAutoFieldsShouldBeFilled() {
        TestMongoVersionedModel savedModel = dao.save(new TestMongoVersionedModel()).block();
        savedModel.setName(randomName());
        TestMongoVersionedModel resavedModel = dao.save(savedModel).block();

        assertThatSystemFieldsAreFilled(resavedModel);
    }

    @Test
    void givenEntityIsNotNew_whenSavingAll_thenAllAutoFieldsShouldBeFilled() {
        TestMongoVersionedModel savedModel = dao.save(new TestMongoVersionedModel()).block();
        savedModel.setName(randomName());
        List<TestMongoVersionedModel> resavedModels = dao.saveAll(singletonList(savedModel))
                .toStream().collect(Collectors.toList());

        assertThat(resavedModels, hasSize(1));
        assertThatSystemFieldsAreFilled(resavedModels.get(0));
    }

    @Test
    void whenFindingById_thenDescriptorShouldBeFilled() {
        TestMongoVersionedModel savedModel = saveATestModel();

        TestMongoVersionedModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    private TestMongoVersionedModel saveATestModel() {
        TestMongoVersionedModel modelToSave = new TestMongoVersionedModel();
        modelToSave.setName(randomName());
        return dao.save(modelToSave).block();
    }

    @NotNull
    private String randomName() {
        return UUID.randomUUID().toString();
    }

    private static Matcher<TestMongoVersionedModel> hasNotNullUuid() {
        return hasProperty("uuid", is(notNullValue()));
    }

    @Test
    void whenFindingById_thenDescriptorInternalIdShouldMatchTheHistoryId() {
        TestMongoVersionedModel savedModel = saveATestModel();

        TestMongoVersionedModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private Matcher<TestMongoVersionedModel> hasUuidConsistentWithId() {
        return new ConsistentUuidIdMatcher();
    }

    @Test
    void whenFindingAll_thenDescriptorShouldBeFilled() {
        saveATestModel();

        List<TestMongoVersionedModel> loadedModels = dao.findAll()
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasNotNullUuid()));
    }

    @Test
    void whenFindingAll_thenDescriptorInternalIdShouldMatchTheEntityId() {
        saveATestModel();

        List<TestMongoVersionedModel> loadedModels = dao.findAll()
                .toStream().collect(Collectors.toList());

        assertThat(loadedModels, everyItem(hasUuidConsistentWithId()));
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorShouldBeFilled() {
        TestMongoVersionedModel savedModel = saveATestModel();

        TestMongoVersionedModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasNotNullUuid());
    }

    @Test
    void whenDeletingByIdAndReturning_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestMongoVersionedModel savedModel = saveATestModel();

        TestMongoVersionedModel loadedModel = dao.deleteByIdAndReturn(savedModel.getId()).block();

        assertThat(loadedModel, hasUuidConsistentWithId());
    }

    private static class ConsistentUuidIdMatcher extends TypeSafeDiagnosingMatcher<TestMongoVersionedModel> {
        @Override
        protected boolean matchesSafely(TestMongoVersionedModel item, Description mismatchDescription) {
            if (item.getUuid() == null) {
                mismatchDescription.appendText("uuid is null");
                return false;
            }
            if (item.getHistoryId() == null) {
                mismatchDescription.appendText("id is null");
                return false;
            }
            if (!Objects.equals(item.getUuid().getInternalId(), item.getHistoryId().toString())) {
                mismatchDescription.appendText("uuid.internalId '")
                        .appendValue(item.getUuid().getInternalId())
                        .appendText("' not equal to history id '")
                        .appendValue(item.getHistoryId().toString());
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("model with uuid.internalId equal to historyId.toString()");
        }
    }
}
