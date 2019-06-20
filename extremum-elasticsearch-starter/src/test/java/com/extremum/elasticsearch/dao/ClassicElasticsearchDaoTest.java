package com.extremum.elasticsearch.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.mapper.BasicJsonObjectMapper;
import com.extremum.common.utils.ModelUtils;
import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchStatusException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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


@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest(classes = ClassicElasticsearchDaoConfiguration.class)
class ClassicElasticsearchDaoTest extends TestWithServices {
    @Autowired
    private ClassicTestElasticsearchModelDao dao;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

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
        model = dao.save(model);

        model.setSeqNo(0L);
        model.setName(UUID.randomUUID().toString());
        try {
            dao.save(model);
            fail("An optimistic failure should occur");
        } catch (ElasticsearchStatusException e) {
            assertThat(e.getMessage(), containsString("version conflict"));
        }
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
        TestElasticsearchModel model = createModelWithExternalDescriptor();
        dao.save(model);

        TestElasticsearchModel resultModel = dao.findById(model.getId()).get();
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

        TestElasticsearchModel resultModel = dao.findById(model.getId()).get();

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenEntityIsCreated_whenFindItWithSearch_thenVersionAndSequenceNumberAndPrimaryTermShouldBeFilled() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);
        client.refresh(TestElasticsearchModel.INDEX);

        List<TestElasticsearchModel> searchResult = dao.search(searchByFullString(model.getId()));
        assertThat(searchResult, hasSize(1));
        TestElasticsearchModel resultModel = searchResult.get(0);

        assertThat(resultModel.getVersion(), is(notNullValue()));
        assertThat(resultModel.getSeqNo(), is(notNullValue()));
        assertThat(resultModel.getPrimaryTerm(), is(notNullValue()));
    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);
        dao.deleteById(model.getId());

        assertThat(dao.existsById(model.getId()), is(false));
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
        client.refresh(TestElasticsearchModel.INDEX);

        List<TestElasticsearchModel> results = dao.search(searchByFullString(uniqueName));
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenADocumentExists_whenSearchingForItByDescriptorExternalId_thenItShouldBeFound() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName(UUID.randomUUID().toString());

        model = dao.save(model);
        client.refresh(TestElasticsearchModel.INDEX);

        List<TestElasticsearchModel> results = dao.search(searchByFullString(model.getUuid().getExternalId()));
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
    }

    @Test
    void givenAnEntityExists_whenPatchingItWithoutParameters_thenThePatchShouldBeApplied() {
        TestElasticsearchModel model = createAModelWithOldName();

        boolean patched = dao.patch(model.getId(), "ctx._source.name = \"new name\"");
        assertThat(patched, is(true));

        TestElasticsearchModel foundModel = dao.findById(model.getId()).get();

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

        TestElasticsearchModel foundModel = dao.findById(model.getId()).get();

        assertThat(foundModel.getName(), is("new name"));
    }

    @Test
    void givenNoEntityExists_whenPatchingIt_thenExceptionShouldBeThrown() {
        assertThrows(ModelNotFoundException.class,
                () -> dao.patch(UUID.randomUUID().toString(), "ctx._source.name = \"new name\""));
    }

    @Test
    void givenAnEntityExists_whenPatchingIt_thenModifiedTimeShouldChange() throws Exception {
        TestElasticsearchModel originalModel = createAModelWithOldName();

        waitForAPalpableTime();

        dao.patch(originalModel.getId(), "ctx._source.name = \"new name\"");

        TestElasticsearchModel foundModel = dao.findById(originalModel.getId()).get();

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

    private void assertThatEntityWasMarkedAsDeleted(TestElasticsearchModel model) {
        Optional<TestElasticsearchModel> foundModelOpt = client.getAsJson(TestElasticsearchModel.INDEX,
                model.getId())
                .map(this::parseJsonWithOurObjectMapper);
        assertThat("Present", foundModelOpt.isPresent(), is(true));

        TestElasticsearchModel parsedModel = foundModelOpt.get();
        assertThat("Marked as deleted", parsedModel.getDeleted(), is(true));
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
    void whenSearching_softDeletionShouldBeRespected() {
        String uniqueName = UUID.randomUUID().toString();
        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestElasticsearchModel> results = dao.search(searchByFullString(uniqueName));

        assertThat(results, hasSize(1));
    }

    @NotNull
    private String searchByFullString(String query) {
        return "*" + query + "*";
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

    private static TestElasticsearchModel createModelWithExternalDescriptor() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(UUID.randomUUID().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(Descriptor.StorageType.ELASTICSEARCH)
                .build();

        model.setUuid(descriptor);

        return model;
    }
}
