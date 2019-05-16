package common.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.utils.ModelUtils;
import models.TestModel;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.shaded.org.apache.commons.lang.math.RandomUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.extremum.common.models.PersistableCommonModel.FIELDS.created;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
public class MongoCommonDaoTest {
    @Autowired
    private TestModelDao dao;

    @Test
    public void testCreateModel() {
        TestModel model = getTestModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        TestModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test(expected = OptimisticLockingFailureException.class)
    public void testCreateModelWithWrongVersion() {
        TestModel model = getTestModel();
        model.setId(new ObjectId(model.getUuid().getInternalId()));
        model.setVersion(123L);
        dao.save(model);
    }

    @Test
    public void testCreateModelList() {
        int modelsToCreate = 10;
        List<TestModel> modelList = Stream
                .generate(MongoCommonDaoTest::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        List<TestModel> createdModelList = dao.saveAll(modelList);
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
        TestModel model = getTestModel();
        dao.save(model);

        TestModel resultModel = dao.findById(model.getId()).get();
        assertEquals(model.getId(), resultModel.getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModel.getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModel.getModified().toEpochSecond());
        assertEquals(model.getVersion(), resultModel.getVersion());
        assertEquals(model.getDeleted(), resultModel.getDeleted());

        resultModel = dao.findById(new ObjectId()).orElse(null);
        assertNull(resultModel);

        TestModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.findById(deletedModel.getId()).orElse(null);
        assertNull(resultModel);
    }

    @Test
    public void testGetByFieldValue() {
        TestModel model = getTestModel();
        dao.save(model);

        List<TestModel> resultModels = dao.listByFieldValue(created.name(), model.getCreated());
        assertEquals(1, resultModels.size());
        assertEquals(model.getId(), resultModels.get(0).getId());
        assertEquals(model.getCreated().toEpochSecond(), resultModels.get(0).getCreated().toEpochSecond());
        assertEquals(model.getModified().toEpochSecond(), resultModels.get(0).getModified().toEpochSecond());
        assertEquals(model.getDeleted(), resultModels.get(0).getDeleted());
        assertEquals(model.getVersion(), resultModels.get(0).getVersion());

        resultModels = dao.listByFieldValue(created.name(), ZonedDateTime.now());
        assertTrue(resultModels.isEmpty());

        TestModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModels = dao.listByFieldValue(created.name(), deletedModel.getCreated());
        assertTrue(resultModels.isEmpty());
    }

    @Test
    public void testGetSelectedFieldsById() {
        TestModel model = getTestModel();
        dao.save(model);

        String[] fields = {created.name()};
        TestModel resultModel = dao.getSelectedFieldsById(model.getId(), fields).orElse(null);
        assertNotNull(resultModel);
        assertNotNull(resultModel.getId());
        assertNotNull(resultModel.getCreated());
        assertNull(resultModel.getModified());
        assertNull(resultModel.getVersion());

        TestModel deletedModel = getDeletedTestModel();
        dao.save(deletedModel);

        resultModel = dao.getSelectedFieldsById(deletedModel.getId(), fields).orElse(null);
        assertNull(resultModel);
    }

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

    @Test
    public void testListByParameters() {
        int initCount = dao.listByParameters(null).size();
        int modelsToCreate = 15;
        // limit = 0 означает выбор всего. Такая проверка выполняется отдельно
        int limit = RandomUtils.nextInt(modelsToCreate - 1) + 1;
        int offset = RandomUtils.nextInt(modelsToCreate);
        int idsSize = RandomUtils.nextInt(modelsToCreate);

        String name = UUID.randomUUID().toString();
        List<ObjectId> createdIds = new ArrayList<>();

        for (int i = 0; i < modelsToCreate; i++) {
            TestModel testModel = getTestModel();
            testModel.name = name;
            dao.save(testModel);
            createdIds.add(testModel.getId());
        }
        int count = dao.listByParameters(null).size();
        assertEquals(initCount + modelsToCreate, count);

        count = dao.listByParameters(Collections.emptyMap()).size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        count = dao.listByParameters(Collections.singletonMap("limit", limit)).size();
        assertEquals(limit, count);

        count = dao.listByParameters(Collections.singletonMap("limit", 0)).size();
        assertEquals(initCount, count);

        count = dao.listByParameters(Collections.singletonMap("offset", offset)).size();
        assertEquals(initCount - offset, count);

        count = dao.listByParameters(Collections.singletonMap("ids", createdIds.subList(0, idsSize))).size();
        assertEquals(idsSize, count);

        count = dao.listByParameters(Collections.singletonMap(TestModel.FIELDS.name.name(), name)).size();
        assertEquals(modelsToCreate, count);
    }

    @Test
    public void testThatSpringDataMagicQueryMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(1));
    }

    @Test
    public void testThatSpringDataMagicQueryMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<TestModel> results = dao.findEvenDeletedByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @Test
    public void testThatSpringDataMagicCounterMethodRespectsDeletedFlag() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countByName(uniqueName), is(1L));
    }

    @Test
    public void testThatSpringDataMagicCounterMethodRespects_SeesSoftlyDeletedRecords_annotation() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        assertThat(dao.countEvenDeletedByName(uniqueName), is(2L));
    }

    @NotNull
    private List<TestModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        TestModel notDeleted = new TestModel();
        notDeleted.name = uniqueName;

        TestModel deleted = new TestModel();
        deleted.name = uniqueName;
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }

    private static TestModel getDeletedTestModel() {
        TestModel model = getTestModel();
        model.setDeleted(true);
        return model;
    }

    private static TestModel getTestModel() {
        TestModel model = new TestModel();
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .modelType(ModelUtils.getModelName(model.getClass()))
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        model.setUuid(descriptor);
        return model;
    }
}