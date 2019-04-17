package descriptor.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoDescriptorDaoTest {
    @Autowired
    private DescriptorDao descriptorDao;

    @Autowired
    private Datastore descriptorsStore;

    @Test
    public void testRetrieveByExternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = MongoDescriptorFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    public void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = MongoDescriptorFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(objectId.toString());
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    public void testRetrieveMapByExternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = MongoDescriptorFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByExternalIds(Collections.singleton(externalId));
        assertEquals(1, retrievedMap.size());
        assertEquals(objectId.toString(), retrievedMap.get(externalId));
    }

    @Test
    public void testRetrieveMapByInternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = MongoDescriptorFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByInternalIds(Collections.singleton(objectId.toString()));
        assertEquals(1, retrievedMap.size());
        assertEquals(externalId, retrievedMap.get(objectId.toString()));
    }

    @Test
    public void testRetrieveFromMongo() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = new Descriptor(DescriptorService.createExternalId(),
                internalId,"test_model", Descriptor.StorageType.MONGO);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertFalse(retrievedDescriptor.isPresent());

        descriptorsStore.save(descriptor);
        retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    public void testSaveDisplayFieldAsNull() {
        String internalId = new ObjectId().toString();
        String externalId = DescriptorService.createExternalId();
        Descriptor descriptor = new Descriptor(externalId, internalId, "test_model",
                Descriptor.StorageType.MONGO, null);

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get().getDisplay());
    }

    @Test
    public void testSaveDisplayFieldAsString() {
        String internalId = new ObjectId().toString();
        String externalId = DescriptorService.createExternalId();
        Descriptor descriptor = new Descriptor(externalId, internalId, "test_model",
                Descriptor.StorageType.MONGO, "abcd");

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertEquals("abcd", retrieved.get().getDisplay());
    }

    @Test
    public void testSaveDisplayFieldAsSerializedString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("key1", "string value");
        json.put("key2", 4);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(1);
        jsonArray.put("abcd");

        json.put("key3", jsonArray);

        String internalId = new ObjectId().toString();
        String externalId = DescriptorService.createExternalId();
        Descriptor descriptor = new Descriptor(externalId, internalId, "test_model",
                Descriptor.StorageType.MONGO, json.toString());

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());

        JSONObject deserializedJson = new JSONObject(retrieved.get().getDisplay());

        assertTrue(deserializedJson.has("key1"));
        assertTrue(deserializedJson.has("key2"));
        assertTrue(deserializedJson.has("key3"));

        assertEquals("string value", deserializedJson.getString("key1"));
        assertEquals(4, deserializedJson.getInt("key2"));

        JSONArray deserializedArray = deserializedJson.getJSONArray("key3");

        assertNotNull(deserializedArray);
        assertEquals(2, deserializedArray.length());
        assertEquals(1, deserializedArray.getInt(0));
        assertEquals("abcd", deserializedArray.getString(1));
    }
}
