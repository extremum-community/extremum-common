package com.extremum.common.dao;

import com.extremum.common.config.AppConfiguration;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.TestModel;
import org.apache.commons.lang.math.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.query.UpdateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = AppConfiguration.class)
public class MongoCommonDaoTest {

    @Autowired
    private TestModelDao dao;

    @Test
    public void testCreateModel() {
        TestModel createdModel = dao.create((TestModel) null);
        assertNull(createdModel);

        TestModel model = getTestModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        createdModel = dao.create(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getVersion());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateModelWithWrongVersion() {
        TestModel model = getTestModel();
        model.setVersion(123L);
        dao.create(model);
    }

    @Test
    public void testCreateModelList() {
        List<TestModel> createdModelList = dao.create((List<TestModel>) null);
        assertNull(createdModelList);

        int modelsToCreate = 10;
        List<TestModel> modelList = Stream
                .generate(MongoCommonDaoTest::getTestModel)
                .limit(modelsToCreate)
                .collect(Collectors.toList());

        createdModelList = dao.create(modelList);
        assertNotNull(createdModelList);
        assertEquals(modelsToCreate, createdModelList.size());

        long validCreated = createdModelList.stream()
                                .filter(model -> modelList.contains(model) && model.getCreated() != null
                                        && model.getVersion() != null && model.getId() != null)
                                .count();
        assertEquals(modelsToCreate, validCreated);
    }

    @Test
    public void testMerge() {
        TestModel mergedModel = dao.merge(null);
        assertNull(mergedModel);

        TestModel model = getTestModel();
        dao.create(model);

        ZonedDateTime initModified = model.getModified();
        mergedModel = dao.merge(model);
        assertTrue(mergedModel.getModified().isAfter(initModified));
    }

    @Test(expected = UpdateException.class)
    public void testMergeNotCreatedModel() {
        dao.merge(getTestModel());
    }

    @Test
    public void testDelete() {
        TestModel deletedModel = dao.delete((ObjectId) null);
        assertNull(deletedModel);

        deletedModel = dao.delete(new ObjectId());
        assertNull(deletedModel);

        TestModel model = getTestModel();
        dao.create(model);

        deletedModel = dao.delete(model.getId());
        assertTrue(deletedModel.getDeleted());
    }

    @Test
    public void testGet() {
        TestModel model = getTestModel();
        dao.create(model);

        TestModel resultModel = dao.get(model.getId());
        assertEquals(model, resultModel);

        resultModel = dao.get(new ObjectId());
        assertNull(resultModel);

        TestModel deletedModel = getDeletedTestModel();
        dao.create(deletedModel);

        resultModel = dao.get(deletedModel.getId());
        assertNull(resultModel);
    }

    @Test
    public void testGetByFieldValue() {
        TestModel model = getTestModel();
        dao.create(model);

        List<TestModel> resultModels = dao.getByFieldValue(MongoCommonModel.FIELDS.created.name(), model.getCreated());
        assertEquals(1, resultModels.size());
        assertEquals(model, resultModels.get(0));

        resultModels = dao.getByFieldValue(MongoCommonModel.FIELDS.created.name(), ZonedDateTime.now());
        assertTrue(resultModels.isEmpty());

        TestModel deletedModel = getDeletedTestModel();
        dao.create(deletedModel);

        resultModels = dao.getByFieldValue(MongoCommonModel.FIELDS.created.name(), deletedModel.getCreated());
        assertTrue(resultModels.isEmpty());
    }

    @Test
    public void testGetSelectedFieldsById() {
        TestModel model = getTestModel();
        dao.create(model);

        TestModel resultModel = dao.getSelectedFieldsById(model.getId(), null);
        assertNull(resultModel);

        String[] fields = {MongoCommonModel.FIELDS.created.name()};
        resultModel = dao.getSelectedFieldsById(model.getId(), fields);
        assertNotNull(resultModel);
        assertNotNull(resultModel.getId());
        assertNotNull(resultModel.getCreated());
        assertNull(resultModel.getModified());
        assertNull(resultModel.getVersion());

        TestModel deletedModel = getDeletedTestModel();
        dao.create(deletedModel);

        resultModel = dao.getSelectedFieldsById(deletedModel.getId(), fields);
        assertNull(resultModel);
    }

    @Test
    public void testListAll() {
        int initCount = dao.listAll().size();
        int modelsToCreate = 10;

        for(int i = 0; i < modelsToCreate; i++) {
            dao.create(getTestModel());
        }
        int count = dao.listAll().size();
        assertEquals(initCount + modelsToCreate, count);

        initCount = count;
        for(int i = 0; i < modelsToCreate; i++) {
            dao.create(getDeletedTestModel());
        }
        count = dao.listAll().size();
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

        ZonedDateTime createTime = ZonedDateTime.now();
        List<ObjectId> createdIds = new ArrayList<>();

        for(int i = 0; i < modelsToCreate; i++) {
            TestModel testModel = getTestModel();
            testModel.setCreated(createTime);
            dao.create(testModel);
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

        count = dao.listByParameters(Collections.singletonMap(MongoCommonModel.FIELDS.created.name(), createTime)).size();
        assertEquals(modelsToCreate, count);
    }

    private static TestModel getDeletedTestModel() {
        TestModel model = getTestModel();
        model.setDeleted(true);
        return model;
    }

    private static TestModel getTestModel() {
        TestModel model = new TestModel();
        Descriptor descriptor = new Descriptor(DescriptorService.createExternalId(), new ObjectId().toString(),
                model.getModelName(), Descriptor.StorageType.MONGO);
        model.setUuid(descriptor);
        return model;
    }
}
