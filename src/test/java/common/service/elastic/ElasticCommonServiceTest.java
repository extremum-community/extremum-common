package common.service.elastic;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.response.Alert;
import com.extremum.common.utils.ModelUtils;
import common.dao.elastic.TestElasticModelDao;
import models.TestElasticModel;
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

    // TODO: restore?
//    @Test
//    void testListWithParameters() {
//        TestElasticModel createdModel = getTestModel();
//        Map<String, Object> params = Collections.singletonMap("offset", 1);
//        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());
//
//        List<TestElasticModel> resultModelList = service.listByParameters(null);
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//
//        resultModelList = service.listByParameters(params);
//        assertNotNull(resultModelList);
//        assertTrue(resultModelList.isEmpty());
//    }
//
//    @Test
//    void testListWithParametersWithAlerts() {
//        TestElasticModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Map<String, Object> params = Collections.singletonMap("offset", 1);
//        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());
//
//        List<TestElasticModel> resultModelList = service.listByParameters(null, alertList);
//        assertTrue(alertList.isEmpty());
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//
//        resultModelList = service.listByParameters(params, alertList);
//        assertTrue(alertList.isEmpty());
//        assertNotNull(resultModelList);
//        assertTrue(resultModelList.isEmpty());
//    }
//
//    @Test
//    void testListByFieldValue() {
//        TestElasticModel createdModel = getTestModel();
//        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
//                .thenReturn(Collections.singletonList(createdModel));
//
//        List<TestElasticModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion());
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testListByFieldValueWithNullName() {
//        service.listByFieldValue(null, "");
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testListByFieldValueWithNullValue() {
//        service.listByFieldValue(version.name(), null);
//    }
//
//    @Test
//    void testListByFieldValueWithAlerts() {
//        TestElasticModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
//                .thenReturn(Collections.singletonList(createdModel));
//
//        List<TestElasticModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), alertList);
//        assertTrue(alertList.isEmpty());
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//
//        resultModelList = service.listByFieldValue(null, null, alertList);
//        assertEquals(2, alertList.size());
//        assertEquals("400", alertList.get(0).getCode());
//        assertEquals("400", alertList.get(1).getCode());
//
//        assertNotNull(resultModelList);
//        assertTrue(resultModelList.isEmpty());
//    }
//
//    @Test
//    void testListByFieldValueWithLimitOffset() {
//        TestElasticModel createdModel = getTestModel();
//        Map<String, Object> params = new HashMap<>();
//        params.put("limit", 1);
//        params.put("offset", 1);
//        params.put(version.name(), createdModel.getVersion());
//
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));
//
//        List<TestElasticModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1);
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testListByFieldValueWithLimitOffsetWithNullName() {
//        service.listByFieldValue(null, "", 0, 0);
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testListByFieldValueWithLimitOffsetWithNullValue() {
//        service.listByFieldValue(version.name(), null, 0, 0);
//    }
//
//    @Test
//    void testListByFieldValueWithLimitOffsetWithAlerts() {
//        TestElasticModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Map<String, Object> params = new HashMap<>();
//        params.put("limit", 1);
//        params.put("offset", 1);
//        params.put(version.name(), createdModel.getVersion());
//
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));
//
//        List<TestElasticModel> resultModelList =
//                service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1, alertList);
//        assertTrue(alertList.isEmpty());
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//
//        resultModelList = service.listByFieldValue(null, null, 0, 0, alertList);
//
//        assertEquals(2, alertList.size());
//        assertTrue(alertList.get(0).isError());
//        assertTrue(alertList.get(1).isError());
//        assertEquals("400", alertList.get(0).getCode());
//        assertEquals("400", alertList.get(1).getCode());
//
//        assertNotNull(resultModelList);
//        assertTrue(resultModelList.isEmpty());
//    }
//
//    @Test
//    void testGetSelectedFieldsById() {
//        TestElasticModel createdModel = getTestModel();
//        Mockito.when(dao.getSelectedFieldsById(createdModel.getId(), version.name())).thenReturn(
//                Optional.of(createdModel));
//
//        TestElasticModel resultModel = service.getSelectedFieldsById(createdModel.getId().toString(), version.name());
//        assertEquals(createdModel, resultModel);
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testGetSelectedFieldsByIdWithNullId() {
//        service.getSelectedFieldsById(null, version.name());
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    void testGetSelectedFieldsByIdWithNullFields() {
//        service.getSelectedFieldsById(new ObjectId().toString());
//    }
//
//    @Test(expected = ModelNotFoundException.class)
//    void testGetSelectedFieldsByIdWithException() {
//        service.getSelectedFieldsById(new ObjectId().toString(), version.name());
//    }
//
//    @Test
//    void testGetSelectedFieldsByIdWithAlerts() {
//        List<Alert> alertList = new ArrayList<>();
//        TestElasticModel resultModel = service.getSelectedFieldsById(new ObjectId().toString(), alertList, version.name());
//
//        assertNull(resultModel);
//        assertEquals(1, alertList.size());
//        assertTrue(alertList.get(0).isError());
//        assertEquals("404", alertList.get(0).getCode());
//
//        alertList = new ArrayList<>();
//        resultModel = service.getSelectedFieldsById(null, alertList);
//
//        assertNull(resultModel);
//        assertEquals(2, alertList.size());
//        assertTrue(alertList.get(0).isError());
//        assertTrue(alertList.get(1).isError());
//        assertEquals("400", alertList.get(0).getCode());
//        assertEquals("400", alertList.get(1).getCode());
//    }

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
