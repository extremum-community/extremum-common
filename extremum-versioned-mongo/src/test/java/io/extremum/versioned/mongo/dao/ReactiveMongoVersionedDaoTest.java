package io.extremum.versioned.mongo.dao;

import io.extremum.mongo.MongoConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static io.extremum.versioned.VersionedModel.FIELDS.historyId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootTest(classes = ReactiveVersionedMongoDaoTestConfiguration.class)
class ReactiveMongoVersionedDaoTest extends TestWithServices {
    @Autowired
    private TestReactiveMongoVersionedDao dao;

    @Autowired
    private MongoOperations mongoOperations;

    @Test
    void givenNoPreviousHistoryExists_whenModelIsSaved_then1SnapshotShouldBeCreated() {
        TestMongoVersionedModel savedModel = saveAModelWithInitialName();

        List<TestMongoVersionedModel> snapshots = findSnapshotsByHistoryId(savedModel);

        assertThat(snapshots, hasSize(1));
        TestMongoVersionedModel snapshot = snapshots.get(0);
        assertThat(snapshot.getId(), is(notNullValue()));
        assertThat(snapshot.getCreated(), is(notNullValue()));
        assertThat(snapshot.getStart(), is(notNullValue()));
        assertThat(snapshot.getStart(), is(equalTo(snapshot.getCreated())));
        assertThat(snapshot.getEnd(), is(MongoConstants.DISTANT_FUTURE));
        assertThat(snapshot.getVersion(), is(0L));
        assertThat(snapshot.getName(), is("Initial name"));
    }

    @NotNull
    private List<TestMongoVersionedModel> findSnapshotsByHistoryId(TestMongoVersionedModel model) {
        return mongoOperations.find(
                    new Query().addCriteria(where(historyId.name()).is(model.getHistoryId())),
                    TestMongoVersionedModel.class);
    }

    @NotNull
    private TestMongoVersionedModel saveAModelWithInitialName() {
        TestMongoVersionedModel model = dao.save(new TestMongoVersionedModel("Initial name")).block();
        assertThat(model, is(notNullValue()));
        return model;
    }

    @Test
    void givenOneSnapshotExists_whenModelIsRetrievedByHistoryId_thenTheSnapshotShouldBeReturned() {
        TestMongoVersionedModel savedModel = saveAModelWithInitialName();

        TestMongoVersionedModel retrievedModel = dao.findById(savedModel.getHistoryId()).block();

        assertThat(retrievedModel, is(notNullValue()));
        assertThat(retrievedModel.getName(), is("Initial name"));
    }

    @Test
    void givenSomeHistoryExists_whenModelIsSaved_then1MoreSnapshotShouldBeAdded() {
        TestMongoVersionedModel savedModel = saveAModelAndThenChangeItsName();

        List<TestMongoVersionedModel> snapshots = findSnapshotsByHistoryId(savedModel);

        assertThat(snapshots, hasSize(2));

        TestMongoVersionedModel firstSnapshot = snapshots.get(0);
        assertThat(firstSnapshot.getId(), is(notNullValue()));

        TestMongoVersionedModel secondSnapshot = snapshots.get(1);
        assertThat(secondSnapshot.getId(), is(notNullValue()));
        assertThat(secondSnapshot.getCreated(), is(notNullValue()));
        assertThat(secondSnapshot.getStart(), is(notNullValue()));
        assertThat(secondSnapshot.getEnd(), is(MongoConstants.DISTANT_FUTURE));
        assertThat(secondSnapshot.getName(), is("Changed name"));

        assertThat(firstSnapshot.getHistoryId(), equalTo(secondSnapshot.getHistoryId()));
        assertThat(firstSnapshot.getCreated(), equalTo(secondSnapshot.getCreated()));
        assertThat(firstSnapshot.getEnd(), equalTo(secondSnapshot.getStart()));
        assertThat(firstSnapshot.getSnapshotId(), not(equalTo(secondSnapshot.getSnapshotId())));
    }

    private TestMongoVersionedModel saveAModelAndThenChangeItsName() {
        TestMongoVersionedModel savedModel = saveAModelWithInitialName();
        savedModel.setName("Changed name");
        return dao.save(savedModel).block();
    }

    @Test
    void givenSomeHistoryExists_whenModelIsWithWrongVersion_thenAnOptimisticLockingExceptionShouldBeTheResult() {
        TestMongoVersionedModel savedModel = saveAModelAndThenChangeItsName();

        savedModel.setName("Another name");
        savedModel.setVersion(0L);

        Mono<TestMongoVersionedModel> mono = dao.save(savedModel);

        StepVerifier.create(mono)
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }

    @Test
    void givenMoreThan1SnapshotsExist_whenModelIsRetrievedByHistoryId_thenTheLastSnapshotShouldBeReturned() {
        TestMongoVersionedModel savedModel = saveAModelAndThenChangeItsName();

        TestMongoVersionedModel retrievedModel = dao.findById(savedModel.getHistoryId()).block();

        assertThat(retrievedModel, is(notNullValue()));
        assertThat(retrievedModel.getName(), is("Changed name"));
    }

    @Test
    void givenSomeHistoryExists_whenModelIsDeleted_thenANewSnapshotShouldBeCreated() {
        TestMongoVersionedModel savedModel = saveAndDeleteModel();

        List<TestMongoVersionedModel> snapshots = findSnapshotsByHistoryId(savedModel);

        assertThat(snapshots, hasSize(2));
        assertThat(snapshots.get(0).getDeleted(), is(false));
        assertThat(snapshots.get(1).getDeleted(), is(true));
    }

    @NotNull
    private TestMongoVersionedModel saveAndDeleteModel() {
        TestMongoVersionedModel savedModel = saveAModelWithInitialName();
        dao.deleteById(savedModel.getHistoryId()).block();
        return savedModel;
    }

    @Test
    void givenSomeHistoryExists_whenModelIsDeleted_thenTheModelShouldNotBeFoundAnymore() {
        TestMongoVersionedModel savedModel = saveAndDeleteModel();

        TestMongoVersionedModel retrievedModel = dao.findById(savedModel.getHistoryId()).block();
        assertThat(retrievedModel, is(nullValue()));
    }
}
