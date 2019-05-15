package common.service.jpa;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.response.Alert;
import com.extremum.common.utils.ModelUtils;
import common.dao.jpa.TestJpaModelDao;
import models.TestJpaModel;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JpaCommonServiceTest {

    private TestJpaModelDao dao = Mockito.mock(TestJpaModelDao.class);
    private TestJpaModelService service = new TestJpaModelService(dao);

    @Test
    public void testGet() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel resultModel = service.get(createdModel.getId().toString());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testGetWithNullId() {
        service.get(null);
    }

    @Test(expected = ModelNotFoundException.class)
    public void testGetWithException() {
        service.get(UUID.randomUUID().toString());
    }

    @Test
    public void testGetWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel resultModel = service.get(createdModel.getId().toString(), alertList);
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
    public void testList() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModelList = service.list();
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    public void testListWithAlerts() {
        TestJpaModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModelList = service.list(alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    // TODO: restore?
//    @Test
//    public void testListWithParameters() {
//        TestJpaModel createdModel = getTestModel();
//        Map<String, Object> params = Collections.singletonMap("offset", 1);
//        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());
//
//        List<TestJpaModel> resultModelList = service.listByParameters(null);
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
//    public void testListWithParametersWithAlerts() {
//        TestJpaModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Map<String, Object> params = Collections.singletonMap("offset", 1);
//        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());
//
//        List<TestJpaModel> resultModelList = service.listByParameters(null, alertList);
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
//    public void testListByFieldValue() {
//        TestJpaModel createdModel = getTestModel();
//        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
//                .thenReturn(Collections.singletonList(createdModel));
//
//        List<TestJpaModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion());
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testListByFieldValueWithNullName() {
//        service.listByFieldValue(null, "");
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testListByFieldValueWithNullValue() {
//        service.listByFieldValue(version.name(), null);
//    }
//
//    @Test
//    public void testListByFieldValueWithAlerts() {
//        TestJpaModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
//                .thenReturn(Collections.singletonList(createdModel));
//
//        List<TestJpaModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), alertList);
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
//    public void testListByFieldValueWithLimitOffset() {
//        TestJpaModel createdModel = getTestModel();
//        Map<String, Object> params = new HashMap<>();
//        params.put("limit", 1);
//        params.put("offset", 1);
//        params.put(version.name(), createdModel.getVersion());
//
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));
//
//        List<TestJpaModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1);
//        assertNotNull(resultModelList);
//        assertEquals(1, resultModelList.size());
//        assertEquals(createdModel, resultModelList.get(0));
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testListByFieldValueWithLimitOffsetWithNullName() {
//        service.listByFieldValue(null, "", 0, 0);
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testListByFieldValueWithLimitOffsetWithNullValue() {
//        service.listByFieldValue(version.name(), null, 0, 0);
//    }
//
//    @Test
//    public void testListByFieldValueWithLimitOffsetWithAlerts() {
//        TestJpaModel createdModel = getTestModel();
//        List<Alert> alertList = new ArrayList<>();
//        Map<String, Object> params = new HashMap<>();
//        params.put("limit", 1);
//        params.put("offset", 1);
//        params.put(version.name(), createdModel.getVersion());
//
//        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));
//
//        List<TestJpaModel> resultModelList =
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
//    public void testGetSelectedFieldsById() {
//        TestJpaModel createdModel = getTestModel();
//        Mockito.when(dao.getSelectedFieldsById(createdModel.getId(), version.name())).thenReturn(
//                Optional.of(createdModel));
//
//        TestJpaModel resultModel = service.getSelectedFieldsById(createdModel.getId().toString(), version.name());
//        assertEquals(createdModel, resultModel);
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testGetSelectedFieldsByIdWithNullId() {
//        service.getSelectedFieldsById(null, version.name());
//    }
//
//    @Test(expected = WrongArgumentException.class)
//    public void testGetSelectedFieldsByIdWithNullFields() {
//        service.getSelectedFieldsById(new ObjectId().toString());
//    }
//
//    @Test(expected = ModelNotFoundException.class)
//    public void testGetSelectedFieldsByIdWithException() {
//        service.getSelectedFieldsById(new ObjectId().toString(), version.name());
//    }
//
//    @Test
//    public void testGetSelectedFieldsByIdWithAlerts() {
//        List<Alert> alertList = new ArrayList<>();
//        TestJpaModel resultModel = service.getSelectedFieldsById(new ObjectId().toString(), alertList, version.name());
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
    public void testCreate() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.create(new TestJpaModel());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateWithNullData() {
        service.create((TestJpaModel) null);
    }

    @Test
    public void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.create(new TestJpaModel(), alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestJpaModel) null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    public void testCreateList() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModels = service.create(Collections.singletonList(new TestJpaModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateListWithNullData() {
        service.create((List<TestJpaModel>) null);
    }

    @Test
    public void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestJpaModel> resultModels = service.create(Collections.singletonList(new TestJpaModel()), alertList);

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestJpaModel>) null, alertList);

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    public void testSaveNewModel() {
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestJpaModel.class))).thenReturn(createdModel);

        TestJpaModel resultModel = service.save(new TestJpaModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    public void testSaveUpdatedModel() {
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

    @Test(expected = WrongArgumentException.class)
    public void testSaveWithNullData() {
        service.save(null);
    }

    @Test
    public void testSaveModelWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestJpaModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestJpaModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestJpaModel resultModel = service.save(updatedModel, alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(updatedModel, resultModel);

        resultModel = service.save(null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test(expected = WrongArgumentException.class)
    public void testDeleteWithNullId() {
        service.delete(null);
    }

    @Test(expected = ModelNotFoundException.class)
    public void testDeleteWithException() {
        service.delete(UUID.randomUUID().toString());
    }


    private static TestJpaModel getTestModel() {
        TestJpaModel model = new TestJpaModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(UUID.randomUUID());

        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(model.getId().toString())
                .modelType(ModelUtils.getModelName(model))
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        model.setUuid(descriptor);

        return model;
    }
}
