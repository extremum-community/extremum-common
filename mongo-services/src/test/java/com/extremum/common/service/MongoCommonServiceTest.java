package com.extremum.common.service;

import com.extremum.common.dao.TestModelDao;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exception.ModelNotFoundException;
import com.extremum.common.exception.WrongArgumentException;
import com.extremum.common.models.TestModel;
import com.extremum.common.response.Alert;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.extremum.common.models.AbstractCommonModel.FIELDS.version;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoCommonServiceTest {

    private TestModelDao dao = mock(TestModelDao.class);
    {
        when(dao.getEntityClass()).thenReturn(TestModel.class);
    }
    private MongoCommonService<TestModel> service = new MongoCommonServiceImpl(dao);

    @Test
    public void testGet() {
        TestModel createdModel = getTestModel();
        when(dao.get(createdModel.getId())).thenReturn(createdModel);

        TestModel resultModel = service.get(createdModel.getId().toString());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testGetWithNullId() {
        service.get(null);
    }

    @Test(expected = ModelNotFoundException.class)
    public void testGetWithException() {
        service.get(new ObjectId().toString());
    }

    @Test
    public void testGetWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel createdModel = getTestModel();
        when(dao.get(createdModel.getId())).thenReturn(createdModel);

        TestModel resultModel = service.get(createdModel.getId().toString(), alertList);
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(new ObjectId().toString(), alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.NOT_FOUND.toString(), alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.get(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
    }

    @Test
    public void testList() {
        TestModel createdModel = getTestModel();
        when(dao.listAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList = service.list();
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    public void testListWithAlerts() {
        TestModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        when(dao.listAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList = service.list(alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    public void testListWithParameters() {
        TestModel createdModel = getTestModel();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestModel> resultModelList = service.listByParameters(null);
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByParameters(params);
        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testListWithParametersWithAlerts() {
        TestModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestModel> resultModelList = service.listByParameters(null, alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByParameters(params, alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testListByFieldValue() {
        TestModel createdModel = getTestModel();
        when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test(expected = WrongArgumentException.class)
    public void testListByFieldValueWithNullName() {
        service.listByFieldValue(null, "");
    }

    @Test(expected = WrongArgumentException.class)
    public void testListByFieldValueWithNullValue() {
        service.listByFieldValue(version.name(), null);
    }

    @Test
    public void testListByFieldValueWithAlerts() {
        TestModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, alertList);
        assertEquals(2, alertList.size());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testListByFieldValueWithLimitOffset() {
        TestModel createdModel = getTestModel();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1);
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test(expected = WrongArgumentException.class)
    public void testListByFieldValueWithLimitOffsetWithNullName() {
        service.listByFieldValue(null, "", 0, 0);
    }

    @Test(expected = WrongArgumentException.class)
    public void testListByFieldValueWithLimitOffsetWithNullValue() {
        service.listByFieldValue(version.name(), null, 0, 0);
    }

    @Test
    public void testListByFieldValueWithLimitOffsetWithAlerts() {
        TestModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModelList =
                service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1, alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, 0, 0, alertList);

        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testGetSelectedFieldsById() {
        TestModel createdModel = getTestModel();
        when(dao.getSelectedFieldsById(createdModel.getId(), new String[]{version.name()})).thenReturn(createdModel);

        TestModel resultModel = service.getSelectedFieldsById(createdModel.getId().toString(), version.name());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testGetSelectedFieldsByIdWithNullId() {
        service.getSelectedFieldsById(null, version.name());
    }

    @Test(expected = WrongArgumentException.class)
    public void testGetSelectedFieldsByIdWithNullFields() {
        service.getSelectedFieldsById(new ObjectId().toString());
    }

    @Test(expected = ModelNotFoundException.class)
    public void testGetSelectedFieldsByIdWithException() {
        service.getSelectedFieldsById(new ObjectId().toString(), version.name());
    }

    @Test
    public void testGetSelectedFieldsByIdWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel resultModel = service.getSelectedFieldsById(new ObjectId().toString(), alertList, version.name());

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.NOT_FOUND.toString(), alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.getSelectedFieldsById(null, alertList);

        assertNull(resultModel);
        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(1).getCode());
    }

    @Test
    public void testCreate() {
        TestModel createdModel = getTestModel();
        when(dao.create(any(TestModel.class))).thenReturn(createdModel);

        TestModel resultModel = service.create(new TestModel());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateWithNullData() {
        service.create((TestModel) null);
    }

    @Test
    public void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel createdModel = getTestModel();
        when(dao.create(any(TestModel.class))).thenReturn(createdModel);

        TestModel resultModel = service.create(new TestModel(), alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestModel) null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
    }

    @Test
    public void testCreateList() {
        TestModel createdModel = getTestModel();
        when(dao.create(anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModels = service.create(Collections.singletonList(new TestModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateListWithNullData() {
        service.create((List<TestModel>) null);
    }

    @Test
    public void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel createdModel = getTestModel();
        when(dao.create(anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestModel> resultModels = service.create(Collections.singletonList(new TestModel()), alertList);

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestModel>) null, alertList);

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
    }

    @Test
    public void testSaveNewModel() {
        TestModel createdModel = getTestModel();
        when(dao.create(any(TestModel.class))).thenReturn(createdModel);

        TestModel resultModel = service.save(new TestModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    public void testSaveUpdatedModel() {
        TestModel createdModel = getTestModel();
        when(dao.get(createdModel.getId())).thenReturn(createdModel);

        TestModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        when(dao.merge(updatedModel)).thenReturn(updatedModel);

        TestModel resultModel = service.save(updatedModel);
        assertEquals(updatedModel, resultModel);
        assertEquals(createdModel.getUuid(), resultModel.getUuid());
    }

    @Test(expected = WrongArgumentException.class)
    public void testSaveWithNullData() {
        service.save(null);
    }

    @Test
    public void testSaveModelWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel createdModel = getTestModel();
        when(dao.get(createdModel.getId())).thenReturn(createdModel);

        TestModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        when(dao.merge(updatedModel)).thenReturn(updatedModel);

        TestModel resultModel = service.save(updatedModel, alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(updatedModel, resultModel);

        resultModel = service.save(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
    }

    @Test
    public void testDelete() {
        TestModel createdModel = getTestModel();
        when(dao.delete(createdModel.getId())).thenReturn(createdModel);

        TestModel resultModel = service.delete(createdModel.getId().toString());
        assertNotNull(resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testDeleteWithNullId() {
        service.delete(null);
    }

    @Test(expected = ModelNotFoundException.class)
    public void testDeleteWithException() {
        service.delete(new ObjectId().toString());
    }

    @Test
    public void testDeleteWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestModel createdModel = getTestModel();
        when(dao.delete(createdModel.getId())).thenReturn(createdModel);

        TestModel resultModel = service.delete(createdModel.getId().toString(), alertList);
        assertNotNull(resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.delete(new ObjectId().toString(), alertList);
        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.NOT_FOUND.toString(), alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.delete(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), alertList.get(0).getCode());
    }


    private static TestModel getTestModel() {
        TestModel model = new TestModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(new ObjectId());

        Descriptor descriptor = new Descriptor(DescriptorService.createExternalId(), model.getId().toString(),
                model.getModelName(), Descriptor.StorageType.MONGO);
        model.setUuid(descriptor);

        return model;
    }
}
