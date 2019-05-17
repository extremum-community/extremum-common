package common.service.mongo;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.exceptions.WrongArgumentException;
import com.extremum.common.response.Alert;
import com.extremum.common.utils.ModelUtils;
import common.dao.mongo.TestMongoModelDao;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.*;

import static com.extremum.common.models.PersistableCommonModel.FIELDS.version;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MongoCommonServiceTest {

    private TestMongoModelDao dao = Mockito.mock(TestMongoModelDao.class);
    private TestMongoModelService service = new TestMongoModelService(dao);

    @Test
    public void testGet() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel resultModel = service.get(createdModel.getId().toString());
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
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel resultModel = service.get(createdModel.getId().toString(), alertList);
        assertEquals(createdModel, resultModel);
        assertTrue(alertList.isEmpty());

        resultModel = service.get(new ObjectId().toString(), alertList);

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
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.list();
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    public void testListWithAlerts() {
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.findAll()).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.list(alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));
    }

    @Test
    public void testListWithParameters() {
        TestMongoModel createdModel = getTestModel();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestMongoModel> resultModelList = service.listByParameters(null);
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByParameters(params);
        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testListWithParametersWithAlerts() {
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = Collections.singletonMap("offset", 1);
        Mockito.when(dao.listByParameters(null)).thenReturn(Collections.singletonList(createdModel));
        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.emptyList());

        List<TestMongoModel> resultModelList = service.listByParameters(null, alertList);
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
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion());
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
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Mockito.when(dao.listByFieldValue(version.name(), createdModel.getVersion()))
                .thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, alertList);
        assertEquals(2, alertList.size());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testListByFieldValueWithLimitOffset() {
        TestMongoModel createdModel = getTestModel();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList = service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1);
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
        TestMongoModel createdModel = getTestModel();
        List<Alert> alertList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("offset", 1);
        params.put(version.name(), createdModel.getVersion());

        Mockito.when(dao.listByParameters(params)).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModelList =
                service.listByFieldValue(version.name(), createdModel.getVersion(), 1, 1, alertList);
        assertTrue(alertList.isEmpty());
        assertNotNull(resultModelList);
        assertEquals(1, resultModelList.size());
        assertEquals(createdModel, resultModelList.get(0));

        resultModelList = service.listByFieldValue(null, null, 0, 0, alertList);

        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());

        assertNotNull(resultModelList);
        assertTrue(resultModelList.isEmpty());
    }

    @Test
    public void testGetSelectedFieldsById() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.getSelectedFieldsById(createdModel.getId(), version.name())).thenReturn(
                Optional.of(createdModel));

        TestMongoModel resultModel = service.getSelectedFieldsById(createdModel.getId().toString(), version.name());
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
        TestMongoModel resultModel = service.getSelectedFieldsById(new ObjectId().toString(), alertList, version.name());

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("404", alertList.get(0).getCode());

        alertList = new ArrayList<>();
        resultModel = service.getSelectedFieldsById(null, alertList);

        assertNull(resultModel);
        assertEquals(2, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertTrue(alertList.get(1).isError());
        assertEquals("400", alertList.get(0).getCode());
        assertEquals("400", alertList.get(1).getCode());
    }

    @Test
    public void testCreate() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.create(new TestMongoModel());
        assertEquals(createdModel, resultModel);
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateWithNullData() {
        service.create((TestMongoModel) null);
    }

    @Test
    public void testCreateWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.create(new TestMongoModel(), alertList);
        assertTrue(alertList.isEmpty());
        assertEquals(createdModel, resultModel);

        resultModel = service.create((TestMongoModel) null, alertList);

        assertNull(resultModel);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    public void testCreateList() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModels = service.create(Collections.singletonList(new TestMongoModel()));
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));
    }

    @Test(expected = WrongArgumentException.class)
    public void testCreateListWithNullData() {
        service.create((List<TestMongoModel>) null);
    }

    @Test
    public void testCreateListWithAlerts() {
        List<Alert> alertList = new ArrayList<>();
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.saveAll(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(createdModel));

        List<TestMongoModel> resultModels = service.create(Collections.singletonList(new TestMongoModel()), alertList);

        assertTrue(alertList.isEmpty());
        assertNotNull(resultModels);
        assertEquals(1, resultModels.size());
        assertEquals(createdModel, resultModels.get(0));

        resultModels = service.create((List<TestMongoModel>) null, alertList);

        assertNull(resultModels);
        assertEquals(1, alertList.size());
        assertTrue(alertList.get(0).isError());
        assertEquals("400", alertList.get(0).getCode());
    }

    @Test
    public void testSaveNewModel() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.save(ArgumentMatchers.any(TestMongoModel.class))).thenReturn(createdModel);

        TestMongoModel resultModel = service.save(new TestMongoModel());
        assertEquals(createdModel, resultModel);

        resultModel = service.save(createdModel);
        assertEquals(createdModel, resultModel);
    }

    @Test
    public void testSaveUpdatedModel() {
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        updatedModel.setUuid(null);
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestMongoModel resultModel = service.save(updatedModel);
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
        TestMongoModel createdModel = getTestModel();
        Mockito.when(dao.findById(createdModel.getId())).thenReturn(Optional.of(createdModel));

        TestMongoModel updatedModel = getTestModel();
        updatedModel.setId(createdModel.getId());
        Mockito.when(dao.save(updatedModel)).thenReturn(updatedModel);

        TestMongoModel resultModel = service.save(updatedModel, alertList);
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

    @Test
    public void whenNullCollectionAlertsIsPassedToGet_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(() -> service.get("id", null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToList_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(() -> service.list(null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToCreate_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.create(new TestMongoModel(), null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToCreateList_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.create(Collections.emptyList(), null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToSave_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.save(new TestMongoModel(), null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToDelete_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.delete("id", null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToListByParameters_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByParameters(Collections.emptyMap(), null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToListByFieldValue_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByFieldValue("key", "value", null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToListByFieldValueWithPaging_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.listByFieldValue("key", "value", 0, 10, null));
    }

    @Test
    public void whenNullCollectionAlertsIsPassedToGetSelectedFieldsById_thenAnExceptionShouldBeThrown() {
        makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(
                () -> service.getSelectedFieldsById("id", (Collection<Alert>) null));
    }

    private void makeSureThatAnExceptionBecauseOfNullAlertsCollectionIsThrown(Runnable runnable) {
        try {
            runnable.run();
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Alerts collection must not be null"));
        }
    }

    private static TestMongoModel getTestModel() {
        TestMongoModel model = new TestMongoModel();

        model.setCreated(ZonedDateTime.now());
        model.setModified(ZonedDateTime.now());
        model.setVersion(1L);
        model.setId(new ObjectId());

        String modelName = ModelUtils.getModelName(model.getClass());

        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(model.getId().toString())
                .modelType(modelName)
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        model.setUuid(descriptor);

        return model;
    }
}
