package io.extremum.elasticsearch.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.mapper.BasicJsonObjectMapper;
import io.extremum.common.utils.ModelUtils;
import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.elasticsearch.ElasticsearchStatusException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class RepositoryBasedReactiveElasticsearchDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestReactiveElasticsearchModelDao dao;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;
    @Autowired
    private DescriptorService descriptorService;

    private TestElasticsearchClient client;

    @BeforeEach
    void createClient() {
        client = new TestElasticsearchClient(elasticsearchProperties);
    }

    @Test
    void testCreateModel() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        assertNull(model.getId());
        assertNull(model.getUuid());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestElasticsearchModel createdModel = dao.save(model).block();
        assertSame(model, createdModel);
        assertThatSystemFieldsAreFilledAfterSave(createdModel);
    }

    private void assertThatSystemFieldsAreFilledAfterSave(TestElasticsearchModel createdModel) {
        assertNotNull(createdModel.getId(), "id");
        assertNotNull(createdModel.getUuid(), "uuid");
        assertNotNull(createdModel.getCreated(), "created");
        assertNotNull(createdModel.getVersion(), "version");
        assertFalse(createdModel.getDeleted(), "deleted");
    }

    @Test
    void whenFinding_thenDescriptorShouldBeFilled() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();

        TestElasticsearchModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel.getUuid(), is(notNullValue()));
    }

    @Test
    void whenFinding_thenDescriptorInternalIdShouldMatchTheEntityId() {
        TestElasticsearchModel savedModel = dao.save(new TestElasticsearchModel()).block();

        TestElasticsearchModel loadedModel = dao.findById(savedModel.getId()).block();

        assertThat(loadedModel.getUuid().getInternalId(), is(equalTo(savedModel.getId())));
    }

    @Test
    void givenAModelHasAnExternallySuppliedDescriptor_whenSavingIt_thenIdShouldBeFilledFromTheDescriptor() {
        TestElasticsearchModel model = createModelWithExternalDescriptor();
        String internalId = model.getUuid().getInternalId();

        assertThat(model.getId(), is(nullValue()));

        dao.save(model).block();

        assertThat(model.getId(), is(internalId));
    }

    @Test
    void whenAnEntityIsSavedTwice_thenTheVersionShouldBecome2() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model = dao.save(model).block();
        model.setName(UUID.randomUUID().toString());
        model = dao.save(model).block();

        assertThat(model.getVersion(), is(2L));
    }

    @Test
    void testCreateModelWithVersionConflict() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model = makeSureModelHasSeqNumberMoreThanZero(model);

        model.setSeqNo(0L);
        model.setName(UUID.randomUUID().toString());
        try {
            dao.save(model).block();
            fail("An optimistic failure should occur");
        } catch (ElasticsearchStatusException e) {
            assertThat(e.getMessage(), containsString("version conflict"));
        }
    }

    @NotNull
    private TestElasticsearchModel makeSureModelHasSeqNumberMoreThanZero(TestElasticsearchModel model) {
        model = dao.save(model).block();
        model = dao.save(model).block();
        return model;
    }

    @Test
    void whenSaveAllIsCalled_thenAllSystemFieldsShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        dao.saveAll(Collections.singletonList(model)).blockLast();

        assertThatSystemFieldsAreFilledAfterSave(model);
    }

    @Test
    void givenAModelHasAnExternallySuppliedDescriptor_whenSavingTheModelWithSaveAll_thenIdShouldBeFilledFromTheDescriptor() {
        TestElasticsearchModel model = createModelWithExternalDescriptor();
        String internalId = model.getUuid().getInternalId();

        assertThat(model.getId(), is(nullValue()));

        dao.saveAll(Collections.singletonList(model)).blockLast();

        assertThat(model.getId(), is(internalId));
    }

    @Test
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestElasticsearchModel> modelList = Stream
                .generate(TestElasticsearchModel::new)
                .limit(modelsToCreate)
                .collect(Collectors.toList());
        modelList.forEach(model -> model.setName(UUID.randomUUID().toString()));

        List<TestElasticsearchModel> createdModelList = dao.saveAll(modelList)
                .toStream().collect(Collectors.toList());
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                .filter(model -> modelList.contains(model) && model.getCreated() != null
                        && model.getVersion() != null && model.getId() != null)
                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    void givenAnEntityIsSaved_whenSavingItAgain_thenTheIdNorDescriptorShouldChange() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());
        dao.save(model).block();

        String externalId = model.getUuid().getExternalId();
        String internalId = model.getId();

        model.setName(UUID.randomUUID().toString());
        dao.save(model).block();

        assertThat(model.getId(), is(equalTo(internalId)));
        assertThat(model.getUuid().getExternalId(), is(equalTo(externalId)));
    }

    @Test
    void givenEntityExists_whenFindById_thenWeShouldFindTheEntity() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        TestElasticsearchModel resultModel = dao.findById(model.getId()).block();
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());
    }

    @Test
    void givenEntityDoesNotExist_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel resultModel = dao.findById(UUID.randomUUID().toString()).block();
        assertNull(resultModel);
    }

    @Test
    void givenEntityIsDeleted_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel modelToBeDeleted = new TestElasticsearchModel();
        dao.save(modelToBeDeleted).block();
        dao.deleteById(modelToBeDeleted.getId()).block();

        TestElasticsearchModel resultModel = dao.findById(modelToBeDeleted.getId()).block();
        assertNull(resultModel);
    }

    @Test
    void whenSavingAnEntity_thenVersionSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        assertThat(model.getVersion(), is(notNullValue()));
        assertThat(model.getSeqNo(), is(notNullValue()));
        assertThat(model.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindById_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        TestElasticsearchModel resultModel = dao.findById(model.getId()).block();

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindItWithSearch_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        List<TestElasticsearchModel> searchResult = dao.search(model.getId(), exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(searchResult, hasSize(1));
        TestElasticsearchModel resultModel = searchResult.get(0);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    private SearchOptions exactMatchSearch() {
        return SearchOptions.builder().exactFieldValueMatch(true).build();
    }

    @Test
    void givenEntityIsCreated_whenFindItWithFindAll_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        List<TestElasticsearchModel> list = dao.findAllById(Collections.singletonList(model.getId()))
                .toStream().collect(Collectors.toList());
        assertThat(list, hasSize(1));
        TestElasticsearchModel resultModel = list.get(0);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void testFindAll_throwsAnException() {
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(createModelWithExternalDescriptor()).block();
        }

        assertThrows(UnsupportedOperationException.class, dao::findAll);
        assertThrows(UnsupportedOperationException.class, () -> dao.findAll(Sort.by("id")));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();
        dao.deleteById(model.getId()).block();

        assertThat(dao.existsById(model.getId()), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();
        dao.deleteById(model.getId()).block();

        Iterable<TestElasticsearchModel> all = dao.findAllById(Collections.singletonList(model.getId()))
                .toIterable();

        assertThat(all.iterator().hasNext(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestElasticsearchModel model = dao.save(new TestElasticsearchModel()).block();

        TestElasticsearchModel deletedModel = dao.deleteByIdAndReturn(model.getId()).block();
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.findByName(uniqueName)
                .toStream().collect(Collectors.toList());
        assertThat(results, hasSize(1));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.findEvenDeletedByName(uniqueName)
                .toStream().collect(Collectors.toList());
        assertThat(results, hasSize(2));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countByName(uniqueName).block(), is(1L));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        assertThat(dao.countEvenDeletedByName(uniqueName).block(), is(2L));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymore() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model).block();

        assertThat(dao.findById(model.getId()).block(), is(notNullValue()));

        dao.deleteById(model.getId()).block();

        assertThat(dao.findById(model.getId()).block(), is(nullValue()));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByName_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        String uniqueName = UUID.randomUUID().toString();
        model.setName(uniqueName);
        
        model = dao.save(model).block();

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByDescriptorExternalId_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        model = dao.save(model).block();

        List<TestElasticsearchModel> results = dao.search(model.getUuid().getExternalId(), exactMatchSearch())
                .toStream().collect(Collectors.toList());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithoutParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = \"new name\"").block();
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId()).block();

        assertThat(foundModel.getName(), is("new name"));
    }

    @NotNull
    private TestElasticsearchModel createAModelWithOldName() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("old name");
        dao.save(model).block();
        return model;
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = params.name",
                Collections.singletonMap("name", "new name")).block();
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId()).block();

        assertThat(foundModel.getName(), is("new name"));
    }

    @Test
    void givenNoEntityExists_whenPatchingIt_thenExceptionShouldBeThrown() {
        assertThrows(ElasticsearchStatusException.class,
                () -> dao.patch(UUID.randomUUID().toString(), "ctx._source.name = \"new name\"").block());
    }

    @Test
    void givenAnEntityExists_whenPatchingIt_thenModifiedTimeShouldChange() throws Exception {
        TestElasticsearchModel originalModel = createAModelWithOldName();

        waitForAPalpableTime();

        dao.patch(originalModel.getId(), "ctx._source.name = \"new name\"").block();

        TestElasticsearchModel foundModel = dao.findById(originalModel.getId()).block();

        assertThatFoundModelModificationTimeIsAfterTheOriginalModelModificationTime(originalModel, foundModel);
    }

    private void waitForAPalpableTime() throws InterruptedException {
        Thread.sleep(100);
    }

    private void assertThatFoundModelModificationTimeIsAfterTheOriginalModelModificationTime(
            TestElasticsearchModel originalModel, TestElasticsearchModel foundModel) {
        ZonedDateTime originalModified = originalModel.getModified();
        ZonedDateTime foundModified = foundModel.getModified();
        assertTrue(foundModified.isAfter(originalModified));
    }

    @Test
    void whenAnEntityIsDeletedByObject_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        dao.delete(model).block();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    private void assertThatEntityWasMarkedAsDeleted(TestElasticsearchModel model) {
        Optional<TestElasticsearchModel> foundModelOpt = client.getAsJson(TestElasticsearchModel.INDEX,
                model.getId())
                .map(this::parseJsonWithOurObjectMapper);
        assertThat("Present", foundModelOpt.isPresent(), is(true));

        TestElasticsearchModel parsedModel = foundModelOpt
                .orElseThrow(this::didNotFindAnything);
        assertThat("Marked as deleted", parsedModel.getDeleted(), is(true));
    }

    @NotNull
    private AssertionError didNotFindAnything() {
        return new AssertionError("Did not find");
    }

    private TestElasticsearchModel parseJsonWithOurObjectMapper(String json) {
        ObjectMapper mapper = new BasicJsonObjectMapper();
        try {
            return mapper.readerFor(TestElasticsearchModel.class).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenAnEntityIsDeletedById_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        dao.deleteById(model.getId()).block();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenAnEntityIsDeletedWithABatch_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model).block();

        dao.deleteAll(ImmutableList.of(model)).block();

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenDeleteAll_thenAnExceptionShouldBeThrown() {
        try {
            dao.deleteAll().block();
            fail("An exception should be thrown");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("We don't allow to delete all the documents in one go"));
        }
    }

    @Test
    void whenSearching_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch())
                .toStream().collect(Collectors.toList());

        assertThat(results, hasSize(1));
    }

    @Test
    void countShouldRespectSoftDeletion() {
        long countBefore = dao.count().block();

        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName)).blockLast();

        long countAfter = dao.count().block();

        assertThat(countAfter - countBefore, is(1L));
    }

    @Test
    void given1ExactFieldMatchAnd2NonExactMatchesExist_whenSearchingWithExactSemantics_then1ResultShouldBeFound() {
        ReactiveElasticsearchExactSearchTests tests = new ReactiveElasticsearchExactSearchTests(dao);
        String exactName = tests.generate1ModelWithExactNameAnd2ModelsWithReversedAndAmendedNamesAndReturnExactName();

        tests.assertThatInexactSearchYields3Results(exactName);
        tests.assertThatExactSearchYields1Result(exactName);
    }

    @NotNull
    private List<TestElasticsearchModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestElasticsearchModel notDeleted = new TestElasticsearchModel();
        notDeleted.setName(uniqueName);

        TestElasticsearchModel deleted = new TestElasticsearchModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    private TestElasticsearchModel createModelWithExternalDescriptor() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(UUID.randomUUID().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(Descriptor.StorageType.ELASTICSEARCH)
                .build();

        model.setUuid(descriptor);

        return model;
    }
}
