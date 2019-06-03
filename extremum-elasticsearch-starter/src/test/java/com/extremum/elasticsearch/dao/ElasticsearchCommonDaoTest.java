package com.extremum.elasticsearch.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.utils.ModelUtils;
import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import org.bson.types.ObjectId;
import org.elasticsearch.ElasticsearchStatusException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(classes = ElasticsearchCommonDaoConfiguration.class)
class ElasticsearchCommonDaoTest extends TestWithServices {
    @Autowired
    private TestElasticsearchModelDao dao;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    private TestElasticsearchClient client;

    @BeforeEach
    void createClient() {
        client = new TestElasticsearchClient(elasticsearchProperties);
    }

    @Test
    void testCreateModel() {
        TestElasticsearchModel model = getTestModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestElasticsearchModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
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
    void testCreateModelWithWrongVersion() {
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
    void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestElasticsearchModel> modelList = Stream
                .generate(ElasticsearchCommonDaoTest::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

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
    void givenEntityExists_whenFindById_thenWeShouldFindTheEntity() {
        TestElasticsearchModel model = getTestModel();
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
    void testFindAll() {
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getTestModel());
        }

        assertEquals(0, dao.findAll().size());
    }

    // TODO: restore
//        assertThat(dao.findAll(Sort.by("id")), hasSize(count));
//
//        assertThat(dao.findAll(Pageable.unpaged()).getTotalElements(), is((long) count));

    // TODO: restore
//    @Test
//    void givenOneExemplarEntityExists_whenInvokingFindAllByExample_thenOneDocumentShouldBeReturned() {
//        TestElasticModel model = dao.save(new TestElasticModel());
//
//        List<TestElasticModel> all = dao.findAll(Example.of(model));
//
//        assertThat(all, hasSize(1));
//        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
//    }

    // TODO: restore
//    @Test
//    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExample_thenNothingShouldBeReturned() {
//        TestElasticModel model = new TestElasticModel();
//        model.setDeleted(true);
//        dao.save(model);
//
//        List<TestElasticModel> all = dao.findAll(Example.of(model));
//
//        assertThat(all, hasSize(0));
//    }

    // TODO: restore
//    @Test
//    void givenOneExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenOneDocumentShouldBeReturned() {
//        TestElasticModel model = dao.save(new TestElasticModel());
//
//        List<TestElasticModel> all = dao.findAll(Example.of(model), Sort.by("id"));
//
//        assertThat(all, hasSize(1));
//        assertThat(all.get(0).getId(), is(equalTo(model.getId())));
//    }

    // TODO: restore
//    @Test
//    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExampleWithSort_thenNothingShouldBeReturned() {
//        TestElasticModel model = dao.save(getDeletedTestModel());
//
//        List<TestElasticModel> all = dao.findAll(Example.of(model), Sort.by("id"));
//
//        assertThat(all, hasSize(0));
//    }

    // TODO: restore
//    @Test
//    void givenADeletedExemplarEntityExists_whenInvokingFindAllByExampleWithPageable_thenNothingShouldBeReturned() {
//        TestElasticModel model = dao.save(getDeletedTestModel());
//
//        Page<TestElasticModel> page = dao.findAll(Example.of(model), Pageable.unpaged());
//
//        assertThat(page.getTotalElements(), is(0L));
//    }

    // TODO: restore
//    @Test
//    void givenADeletedExemplarEntityExists_whenInvokingFindOneByExample_thenNothingShouldBeReturned() {
//        TestElasticModel model = dao.save(getDeletedTestModel());
//
//        Optional<TestElasticModel> result = dao.findOne(Example.of(model));
//
//        assertThat(result.isPresent(), is(false));
//    }

    // TODO: restore
//    @Test
//    void givenADeletedExemplarEntityExists_whenInvokingExistsByExample_thenFalseShouldBeReturned() {
//        TestElasticModel model = dao.save(getDeletedTestModel());
//
//        assertThat(dao.exists(Example.of(model)), is(false));
//    }

    @Test
    void givenADeletedEntityExists_whenInvokingExistsById_thenFalseShouldBeReturned() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        dao.save(model);
        dao.deleteById(model.getId());

        assertThat(dao.existsById(model.getId()), is(false));
    }

    // TODO: restore
//    @Test
//    void givenADeletedEntityExists_whenInvokingFindAllById_thenNothingShouldBeReturned() {
//        TestElasticModel model = dao.save(getDeletedTestModel());
//
//        Iterable<TestElasticModel> all = dao.findAllById(Collections.singletonList(model.getId()));
//
//        assertThat(all.iterator().hasNext(), is(false));
//    }

    // TODO: restore
//    @Test
//    void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
//        String uniqueName = UUID.randomUUID().toString();
//
//        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));
//
//        List<TestElasticModel> results = dao.findByName(uniqueName);
//        assertThat(results, hasSize(1));
//    }

    // TODO: restore
//    @Test
//    void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
//        String uniqueName = UUID.randomUUID().toString();
//
//        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));
//
//        List<TestElasticModel> results = dao.findEvenDeletedByName(uniqueName);
//        assertThat(results, hasSize(2));
//    }

    // TODO: restore
//    @Test
//    void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
//        String uniqueName = UUID.randomUUID().toString();
//
//        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));
//
//        assertThat(dao.countByName(uniqueName), is(1L));
//    }

    // TODO: restore
//    @Test
//    void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
//        String uniqueName = UUID.randomUUID().toString();
//
//        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));
//
//        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
//    }

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
    void givenADocumentExists_whenSearchingForItByName_thenItShouldBeFound() throws Exception {
        TestElasticsearchModel model = new TestElasticsearchModel();
        String uniqueName = UUID.randomUUID().toString().replaceAll("-", "");
        model.setName(uniqueName);
        
        model = dao.save(model);
        client.refresh(TestElasticsearchModel.INDEX);

        List<TestElasticsearchModel> results = dao.search(uniqueName);
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getName(), is(equalTo(model.getName())));
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

    private static TestElasticsearchModel getTestModel() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(Descriptor.StorageType.ELASTICSEARCH)
                .build();

        model.setUuid(descriptor);
        return model;
    }
}
