package com.extremum.elastic.service;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.response.Alert;
import com.extremum.common.utils.ModelUtils;
import com.extremum.elastic.dao.TestElasticModelDao;
import com.extremum.elastic.model.TestElasticModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticCommonServiceTest {

    private TestElasticModelDao dao = Mockito.mock(TestElasticModelDao.class);
    private TestElasticModelService service = new TestElasticModelService(dao);

    private static TestElasticModel getTestModel() {
        TestElasticModel model = new TestElasticModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(UUID.randomUUID().toString());

        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(model.getId())
                .modelType(ModelUtils.getModelName(model))
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        model.setUuid(descriptor);

        return model;
    }

    @Test
    void testGet() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticModel resultModel = service.get(createdModel.getId());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testGetWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.get(null));
    }

    @Test
    void testGetWithException() {
        assertThrows(ModelNotFoundException.class, () -> service.get(UUID.randomUUID().toString()));
    }

    @Test
    void testGetWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticModel resultModel = service.get(createdModel.getId(), alertList);
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(UUID.randomUUID().toString(), alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("404", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.get(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testList() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticModel> resultModelList = service.list();
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testListWithAlerts() {
        TestElasticModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticModel> resultModelList = service.list(alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testCreate() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestElasticModel.class))).thenReturn(createdModel);

        TestElasticModel resultModel = service.create(new TestElasticModel());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestElasticModel) null));
    }

    @Test
    void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestElasticModel.class))).thenReturn(createdModel);

        TestElasticModel resultModel = service.create(new TestElasticModel(), alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestElasticModel) null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testCreateList() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticModel> resultModels = service.create(Collections.singletonList(new TestElasticModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((List<TestElasticModel>) null));
    }

    @Test
    void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestElasticModel> resultModels = service.create(Collections.singletonList(new TestElasticModel()), alertList);

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestElasticModel>) null, alertList);

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testSaveNewModel() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestElasticModel.class))).thenReturn(createdModel);

        TestElasticModel resultModel = service.save(new TestElasticModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestElasticModel resultModel = service.save(updatedModel);
        assertEquals(updatedModel, resultModel);
        assertEquals(createdModel.getUuid(), resultModel.getUuid());
    }

    @Test
    void testSaveWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.save(null));
    }

    @Test
    void testSaveModelWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestElasticModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestElasticModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestElasticModel resultModel = service.save(updatedModel, alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(updatedModel, resultModel);

        resultModel = service.save(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testDeleteWithNullId() {
        assertThrows(WrongArgumentException.class, () -> service.delete(null));
    }
}
