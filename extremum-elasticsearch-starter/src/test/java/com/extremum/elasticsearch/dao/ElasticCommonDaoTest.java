package com.extremum.elasticsearch.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.utils.ModelUtils;
import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticModel;
import org.bson.types.ObjectId;
import org.elasticsearch.ElasticsearchStatusException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(classes = ElasticCommonDaoConfiguration.class)
class ElasticCommonDaoTest extends TestWithServices {
    @Autowired
    private TestElasticModelDao dao;

    @Test
    void testCreateModel() {
        TestElasticModel model = getTestModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestElasticModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    void whenAnEntityIsSavedTwice_thenTheVersionShouldBecome2() {
        TestElasticModel model = new TestElasticModel();
        model = dao.save(model);
        model.setName(UUID.randomUUID().toString());
        model = dao.save(model);

        assertThat(model.getVersion(), is(2L));
    }

    @Test
    void testCreateModelWithWrongVersion() {
        TestElasticModel model = new TestElasticModel();
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
        List<TestElasticModel> modelList = Stream
                .generate(ElasticCommonDaoTest::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestElasticModel> createdModelList = dao.saveAll(modelList);
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
        TestElasticModel model = getTestModel();
        dao.save(model);

        TestElasticModel resultModel = dao.findById(model.getId()).get();
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());
    }

    @Test
    void givenEntityDoesNotExist_whenFindById_thenNothingShouldBeFound() {
        TestElasticModel resultModel = dao.findById(UUID.randomUUID().toString()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    void givenEntityIsDeleted_whenFindById_thenNothingShouldBeFound() {
        TestElasticModel modelToBeDeleted = new TestElasticModel();
        dao.save(modelToBeDeleted);
        dao.deleteById(modelToBeDeleted.getId());

        TestElasticModel resultModel = dao.findById(modelToBeDeleted.getId()).orElse(null);
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
        TestElasticModel model = new TestElasticModel();
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
        TestElasticModel model = new TestElasticModel();
        model.setName("Test");
        model = dao.save(model);

        assertThat(dao.findById(model.getId()).isPresent(), is(true));

        dao.deleteById(model.getId());

        assertThat(dao.findById(model.getId()).isPresent(), is(false));
    }

    @NotNull
    private List<TestElasticModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestElasticModel notDeleted = new TestElasticModel();
        notDeleted.setName(uniqueName);

        TestElasticModel deleted = new TestElasticModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    private static TestElasticModel getTestModel() {
        TestElasticModel model = new TestElasticModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(Descriptor.StorageType.ELASTIC)
                .build();

        model.setUuid(descriptor);
        return model;
    }
}
