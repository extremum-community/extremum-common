package common.dao.jpa;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import models.TestJpaModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class JpaCommonDaoTest {
    @Autowired
    private TestJpaModelDao dao;

    @Test
    public void testCreateModel() {
        TestJpaModel model = new TestJpaModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestJpaModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    public void testCreateModelWithWrongVersion() {
        TestJpaModel model = getTestModel();
        model = dao.save(model);
        model.name = UUID.randomUUID().toString();
        model = dao.save(model);

        assertThat(model.getVersion(), is(1L));

        model.setVersion(0L);
        try {
            dao.save(model);
            fail("An optimistick locking failure should have occured");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }
    }

    @Test
    public void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestJpaModel> modelList = Stream
                .generate(JpaCommonDaoTest::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestJpaModel> createdModelList = dao.saveAll(modelList);
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                .filter(model -> modelList.contains(model) && model.getCreated() != null
                        && model.getVersion() != null && model.getId() != null)
                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    public void testGet() {
        TestJpaModel model = getTestModel();
        dao.save(model);

        TestJpaModel resultModel = dao.findById(model.getId()).get();
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(UUID.randomUUID()).orElse(null);
        assertNull(resultModel);

        TestJpaModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.findById(deletedModel.getId()).orElse(null);
        assertNull(resultModel);
    }

    // TODO: restore?
//    @Test
//    public void testGetByFieldValue() {
//        TestJpaModel model = getTestModel();
//        dao.save(model);
//
//        List<TestJpaModel> resultModels = dao.listByFieldValue(created.name(), model.getCreated());
//        assertEquals(1, resultModels.size());
//        assertEquals(model.getId(), resultModels.get(0).getId());
//        assertEquals(model.getCreated().toEpochSecond(), resultModels.get(0).getCreated().toEpochSecond());
//        assertEquals(model.getModified().toEpochSecond(), resultModels.get(0).getModified().toEpochSecond());
//        assertEquals(model.getDeleted(), resultModels.get(0).getDeleted());
//        assertEquals(model.getVersion(), resultModels.get(0).getVersion());
//
//        resultModels = dao.listByFieldValue(created.name(), ZonedDateTime.now());
//        assertTrue(resultModels.isEmpty());
//
//        TestJpaModel deletedModel = getDeletedTestModel();
//        dao.save(deletedModel);
//
//        resultModels = dao.listByFieldValue(created.name(), deletedModel.getCreated());
//        assertTrue(resultModels.isEmpty());
//    }

    // TODO: restore?
//    @Test
//    public void testGetSelectedFieldsById() {
//        TestJpaModel model = getTestModel();
//        dao.save(model);
//
//        String[] fields = {created.name()};
//        TestJpaModel resultModel = dao.getSelectedFieldsById(model.getId(), fields).orElse(null);
//        assertNotNull(resultModel);
//        assertNotNull(resultModel.getId());
//        assertNotNull(resultModel.getCreated());
//        assertNull(resultModel.getModified());
//        assertNull(resultModel.getVersion());
//
//        TestJpaModel deletedModel = getDeletedTestModel();
//        dao.save(deletedModel);
//
//        resultModel = dao.getSelectedFieldsById(deletedModel.getId(), fields).orElse(null);
//        assertNull(resultModel);
//    }

    @Test
    public void testListAll() {
        int initCount = dao.findAll().size();
        int modelsToCreate = 10;

        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getTestModel());
        }
        int count = dao.findAll().size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        for (int i = 0; i < modelsToCreate; i++) {
            dao.save(getDeletedTestModel());
        }
        count = dao.findAll().size();
        assertEquals(initCount, count);
    }

    // TODO: restore?
//    @Test
//    public void testListByParameters() {
//        int initCount = dao.listByParameters(null).size();
//        int modelsToCreate = 15;
//        // limit = 0 означает выбор всего. Такая проверка выполняется отдельно
//        int limit = RandomUtils.nextInt(modelsToCreate - 1) + 1;
//        int offset = RandomUtils.nextInt(modelsToCreate);
//        int idsSize = RandomUtils.nextInt(modelsToCreate);
//
//        String name = UUID.randomUUID().toString();
//        List<ObjectId> createdIds = new ArrayList<>();
//
//        for (int i = 0; i < modelsToCreate; i++) {
//            TestJpaModel testModel = getTestModel();
//            testModel.name = name;
//            dao.save(testModel);
//            createdIds.add(testModel.getId());
//        }
//        int count = dao.listByParameters(null).size();
//        assertEquals(initCount + modelsToCreate, count);
//
//        count = dao.listByParameters(Collections.emptyMap()).size();
//        assertEquals(initCount + modelsToCreate, count);
//
//        initCount = count;
//        count = dao.listByParameters(Collections.singletonMap("limit", limit)).size();
//        assertEquals(limit, count);
//
//        count = dao.listByParameters(Collections.singletonMap("limit", 0)).size();
//        assertEquals(initCount, count);
//
//        count = dao.listByParameters(Collections.singletonMap("offset", offset)).size();
//        assertEquals(initCount - offset, count);
//
//        count = dao.listByParameters(Collections.singletonMap("ids", createdIds.subList(0, idsSize))).size();
//        assertEquals(idsSize, count);
//
//        count = dao.listByParameters(Collections.singletonMap(TestJpaModel.FIELDS.name.name(), name)).size();
//        assertEquals(modelsToCreate, count);
//    }

    @Test
    public void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestJpaModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(1));
    }

    @Test
    @Ignore("Restore when we have a decent mechanism to ignore softly-deleted records on Spring Data level")
    public void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestJpaModel> results = dao.findEvenDeletedByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @Test
    public void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(1L));
    }

    @Test
    @Ignore("Restore when we have a decent mechanism to ignore softly-deleted records on Spring Data level")
    public void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
    }

    @NotNull
    private List<TestJpaModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestJpaModel notDeleted = new TestJpaModel();
        notDeleted.name = uniqueName;

        TestJpaModel deleted = new TestJpaModel();
        deleted.name = uniqueName;
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    private static TestJpaModel getDeletedTestModel() {
        TestJpaModel model = getTestModel();
        model.setDeleted(true);
        return model;
    }

    private static TestJpaModel getTestModel() {
        TestJpaModel model = new TestJpaModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(UUID.randomUUID().toString())
                .modelType(model.getModelName())
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        model.setUuid(descriptor);
        return model;
    }
}