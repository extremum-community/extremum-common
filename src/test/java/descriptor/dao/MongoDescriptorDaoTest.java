package descriptor.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.stucts.Display;
import com.extremum.common.stucts.IntegerOrString;
import com.extremum.common.stucts.Media;
import com.extremum.common.stucts.MediaType;
import com.extremum.common.stucts.MultilingualObject;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
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
        Descriptor descriptor = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();

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
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get().getDisplay());
    }

    @Test
    public void testSaveDisplayFieldAsString() {
        String internalId = new ObjectId().toString();
        String externalId = DescriptorService.createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .display(new Display("abcd"))
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().getDisplay().isString());
        assertEquals("abcd", retrieved.get().getDisplay().getStringValue());
    }

    @Test
    public void testSaveDisplayFieldAsSerializedString() throws JSONException {
        Media iconObj = new Media();
        iconObj.setUrl("/url/to/resource");
        iconObj.setType(MediaType.IMAGE);
        iconObj.setWidth(100);
        iconObj.setHeight(200);
        iconObj.setDepth(2);
        iconObj.setDuration(new IntegerOrString(20));

        Display displayObj = new Display(
                new MultilingualObject("aaa"),
                iconObj,
                iconObj
        );

        String internalId = new ObjectId().toString();
        String externalId = DescriptorService.createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .display(displayObj)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());

        Descriptor retrievedDescriptor = retrieved.get();
        Display display = retrievedDescriptor.getDisplay();

        assertNotNull(display);
        assertTrue(display.isObject());
        Media icon = display.getIcon();
        assertNotNull(icon);

        assertEquals("/url/to/resource", icon.getUrl());
        assertEquals(MediaType.IMAGE, icon.getType());
        assertEquals(100, (int) icon.getWidth());
        assertEquals(200, (int) icon.getHeight());
        assertEquals(2, (int) icon.getDepth());
        assertNotNull(icon.getDuration());
        assertTrue(icon.getDuration().isInteger());
        assertEquals(20, (int) icon.getDuration().getIntegerValue());

        Media splash = display.getSplash();
        assertNotNull(splash);

        assertEquals("/url/to/resource", splash.getUrl());
        assertEquals(MediaType.IMAGE, splash.getType());
        assertEquals(100, (int) splash.getWidth());
        assertEquals(200, (int) splash.getHeight());
        assertEquals(2, (int) splash.getDepth());
        assertNotNull(splash.getDuration());
        assertTrue(splash.getDuration().isInteger());
        assertEquals(20, (int) splash.getDuration().getIntegerValue());
    }
}
