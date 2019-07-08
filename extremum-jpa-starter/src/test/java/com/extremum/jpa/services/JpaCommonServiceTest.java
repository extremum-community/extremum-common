package com.extremum.jpa.services;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.response.Alert;
import com.extremum.common.service.AlertsCollector;
import com.extremum.common.utils.ModelUtils;
import com.extremum.common.uuid.StandardUUIDGenerator;
import com.extremum.common.uuid.UUIDGenerator;
import com.extremum.jpa.dao.TestJpaModelDao;
import com.extremum.jpa.models.TestJpaModel;
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

class JpaCommonServiceTest {

    private final TestJpaModelDao dao = Mockito.mock(TestJpaModelDao.class);
    private final TestJpaModelService service = new TestJpaModelService(dao);

    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    private TestJpaModel getTestModel() {
        TestJpaModel model = new TestJpaModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(UUID.randomUUID());

        Descriptor descriptor = Descriptor.builder()
                .externalId(uuidGenerator.generateUUID())
                .internalId(model.getId().toString())
                .modelType(ModelUtils.getModelName(model))
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        model.setUuid(descriptor);

        return model;
    }

    @Test
    void testGet() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel resultModel = service.get(createdModel.getId().toString());
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
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel resultModel = service.get(createdModel.getId().toString(), new AlertsCollector(alertList));
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(UUID.randomUUID().toString(), new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("404", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.get(null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testList() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModelList = service.list();
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testListWithAlerts() {
        TestJpaModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModelList = service.list(new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    void testCreate() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.create(new TestJpaModel());
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testCreateWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((TestJpaModel) null));
    }

    @Test
    void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.create(new TestJpaModel(), new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestJpaModel) null, new AlertsCollector(alertList));

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testCreateList() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModels = service.create(Collections.singletonList(new TestJpaModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test
    void testCreateListWithNullData() {
        assertThrows(WrongArgumentException.class, () -> service.create((List<TestJpaModel>) null));
    }

    @Test
    void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModels = service.create(Collections.singletonList(new TestJpaModel()), new AlertsCollector(alertList));

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestJpaModel>) null, new AlertsCollector(alertList));

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    void testSaveNewModel() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.save(new TestJpaModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    void testSaveUpdatedModel() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestJpaModel resultModel = service.save(updatedModel);
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
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestJpaModel resultModel = service.save(updatedModel, new AlertsCollector(alertList));
        assertTrue(alertList.isEmpty());
        assertEquals(updatedModel, resultModel);

        resultModel = service.save(null, new AlertsCollector(alertList));

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
