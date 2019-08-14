package io.extremum.elasticsearch.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.mapper.BasicJsonObjectMapper;
import io.extremum.common.utils.ModelUtils;
import io.extremum.common.utils.StreamUtils;
import io.extremum.elasticsearch.TestWithServices;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class RepositoryBasedElasticsearchDaoTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestElasticsearchModelDao dao;
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

        TestElasticsearchModel createdModel = dao.save(model);
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
    void givenAModelHasAnExternallySuppliedDescriptor_whenSavingIt_thenIdShouldBeFilledFromTheDescriptor() {
        TestElasticsearchModel model = createModelWithExternalDescriptor();
        String internalId = model.getUuid().getInternalId();

        assertThat(model.getId(), is(nullValue()));

        dao.save(model);

        assertThat(model.getId(), is(internalId));
    }

    @Test
    void whenAnEntityIsSavedTwice_thenTheVersionShouldBecome2() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model = dao.save(model);
        model.setName(UUID.randomUUID().toString());
        model = dao.save(model);

        assertThat(model.getVersion(), is(2L));
    }

    @Test
    void testCreateModelWithVersionConflict() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model = makeSureModelHasSeqNumberMoreThanZero(model);

        model.setSeqNo(0L);
        model.setName(UUID.randomUUID().toString());
        try {
            dao.save(model);
            fail("An optimistic failure should occur");
        } catch (ElasticsearchStatusException e) {
            assertThat(e.getMessage(), containsString("version conflict"));
        }
    }

    @NotNull
    private TestElasticsearchModel makeSureModelHasSeqNumberMoreThanZero(TestElasticsearchModel model) {
        model = dao.save(model);
        model = dao.save(model);
        return model;
    }

    @Test
    void whenSaveAllIsCalled_thenAllSystemFieldsShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        dao.saveAll(Collections.singletonList(model));

        assertThatSystemFieldsAreFilledAfterSave(model);
    }

    @Test
    void givenAModelHasAnExternallySuppliedDescriptor_whenSavingTheModelWithSaveAll_thenIdShouldBeFilledFromTheDescriptor() {
        TestElasticsearchModel model = createModelWithExternalDescriptor();
        String internalId = model.getUuid().getInternalId();

        assertThat(model.getId(), is(nullValue()));

        dao.saveAll(Collections.singletonList(model));

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

        List<TestElasticsearchModel> createdModelList = dao.saveAll(modelList);
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
        dao.save(model);

        String externalId = model.getUuid().getExternalId();
        String internalId = model.getId();

        model.setName(UUID.randomUUID().toString());
        dao.save(model);

        assertThat(model.getId(), is(equalTo(internalId)));
        assertThat(model.getUuid().getExternalId(), is(equalTo(externalId)));
    }

    @Test
    void givenEntityExists_whenFindById_thenWeShouldFindTheEntity() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);

        TestElasticsearchModel resultModel = dao.findById(model.getId())
                .orElseThrow(this::didNotFindAnything);
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());
    }

    @Test
    void givenEntityDoesNotExist_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel resultModel = dao.findById(UUID.randomUUID().toString()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void givenEntityIsDeleted_whenFindById_thenNothingShouldBeFound() {
        TestElasticsearchModel modelToBeDeleted = new TestElasticsearchModel();
        dao.save(modelToBeDeleted);
        dao.deleteById(modelToBeDeleted.getId());

        TestElasticsearchModel resultModel = dao.findById(modelToBeDeleted.getId()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void whenSavingAnEntity_thenVersionSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);

        assertThat(model.getVersion(), is(notNullValue()));
        assertThat(model.getSeqNo(), is(notNullValue()));
        assertThat(model.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindById_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);

        TestElasticsearchModel resultModel = dao.findById(model.getId())
                .orElseThrow(this::didNotFindAnything);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindItWithSearch_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);

        List<TestElasticsearchModel> searchResult = dao.search(model.getId(), exactMatchSearch());
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
        dao.save(model);

        Iterable<TestElasticsearchModel> iterable = dao.findAllById(Collections.singletonList(model.getId()));
        List<TestElasticsearchModel> list = iterableToList(iterable);
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
            dao.save(createModelWithExternalDescriptor());
        }

        assertThrows(UnsupportedOperationException.class, dao::findAll);
        assertThrows(UnsupportedOperationException.class, () -> dao.findAll(Sort.by("id")));
    }

    @Test
    void testFindAllWithPageable_respectsSoftDeletion() {
        long totalBefore = dao.findAll(Pageable.unpaged()).getTotalElements();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(UUID.randomUUID().toString()));

        long totalAfter = dao.findAll(Pageable.unpaged()).getTotalElements();
        assertThat(totalAfter - totalBefore, is(1L));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);
        dao.deleteById(model.getId());

        assertThat(dao.existsById(model.getId()), is(false));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);
        dao.deleteById(model.getId());

        Iterable<TestElasticsearchModel> all = dao.findAllById(Collections.singletonList(model.getId()));

        assertThat(all.iterator().hasNext(), is(false));
    }

    @Test
    void givenEntityExists_whenCallingDeleteByIdAndReturn_thenItShouldBeReturnedAndShouldNotBeFoundLater() {
        TestElasticsearchModel model = dao.save(new TestElasticsearchModel());

        TestElasticsearchModel deletedModel = dao.deleteByIdAndReturn(model.getId());
        assertThat(deletedModel.getId(), is(equalTo(model.getId())));

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestElasticsearchModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(1));
    }

    @Test
    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestElasticsearchModel> results = dao.findEvenDeletedByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(1L));
    }

    @Test
    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
    }

    @Test
    void givenADocumentExists_whenItIsSoftDeleted_thenItShouldNotBeFoundAnymore() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("Test");
        model = dao.save(model);

        assertThat(dao.findById(model.getId()).isPresent(), is(true));

        dao.deleteById(model.getId());

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByName_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        String uniqueName = UUID.randomUUID().toString();
        model.setName(uniqueName);
        
        model = dao.save(model);

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByDescriptorExternalId_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        model = dao.save(model);

        List<TestElasticsearchModel> results = dao.search(model.getUuid().getExternalId(), exactMatchSearch());
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithoutParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = \"new name\"");
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId())
                .orElseThrow(this::didNotFindAnything);

        assertThat(foundModel.getName(), is("new name"));
    }

    @NotNull
    private TestElasticsearchModel createAModelWithOldName() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("old name");
        dao.save(model);
        return model;
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = params.name",
                Collections.singletonMap("name", "new name"));
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId())
                .orElseThrow(this::didNotFindAnything);

        assertThat(foundModel.getName(), is("new name"));
    }

    @Test
    void givenNoEntityExists_whenPatchingIt_thenExceptionShouldBeThrown() {
        assertThrows(ElasticsearchStatusException.class,
                () -> dao.patch(UUID.randomUUID().toString(), "ctx._source.name = \"new name\""));
    }

    @Test
    void givenAnEntityExists_whenPatchingIt_thenModifiedTimeShouldChange() throws Exception {
        TestElasticsearchModel originalModel = createAModelWithOldName();

        waitForAPalpableTime();

        dao.patch(originalModel.getId(), "ctx._source.name = \"new name\"");

        TestElasticsearchModel foundModel = dao.findById(originalModel.getId())
                .orElseThrow(this::didNotFindAnything);

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
        dao.save(model);

        dao.delete(model);

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
        dao.save(model);

        dao.deleteById(model.getId());

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenAnEntityIsDeletedWithABatch_thenItShouldBeMarkedAsDeleted() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);

        dao.deleteAll(ImmutableList.of(model));

        assertThatEntityWasMarkedAsDeleted(model);
    }

    @Test
    void whenDeleteAll_thenAnExceptionShouldBeThrown() {
        try {
            dao.deleteAll();
            fail("An exception should be thrown");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("We don't allow to delete all the documents in one go"));
        }
    }

    @Test
    void whenSearching_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestElasticsearchModel> results = dao.search(uniqueName, exactMatchSearch());

        assertThat(results, hasSize(1));
    }

    @Test
    void whenSearchingWithQueryBuilder_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(uniqueName).analyzer("keyword");
        Iterable<TestElasticsearchModel> iterable = dao.search(query);
        List<TestElasticsearchModel> results = iterableToList(iterable);

        assertThat(results, hasSize(1));
    }

    @Test
    void whenSearchingWithQueryBuilderAndPageable_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(uniqueName).analyzer("keyword");
        Iterable<TestElasticsearchModel> iterable = dao.search(query, Pageable.unpaged());
        List<TestElasticsearchModel> results = iterableToList(iterable);

        assertThat(results, hasSize(1));
    }

    @NotNull
    private <T> List<T> iterableToList(Iterable<T> iterable) {
        return StreamUtils.fromIterable(iterable).collect(Collectors.toList());
    }

    @Test
    void whenSearchingWithSearchQuery_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(uniqueName).analyzer("keyword");
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
        Iterable<TestElasticsearchModel> iterable = dao.search(query);
        List<TestElasticsearchModel> results = iterableToList(iterable);

        assertThat(results, hasSize(1));
    }

    @Test
    void countShouldRespectSoftDeletion() {
        long countBefore = dao.count();

        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        long countAfter = dao.count();

        assertThat(countAfter - countBefore, is(1L));
    }

    @Test
    void given1ExactFieldMatchAnd2NonExactMatchesExist_whenSearchingWithExactSemantics_then1ResultShouldBeFound() {
        ElasticsearchExactSearchTests tests = new ElasticsearchExactSearchTests(dao);
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