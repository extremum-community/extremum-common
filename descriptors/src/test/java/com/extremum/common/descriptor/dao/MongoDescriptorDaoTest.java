package com.extremum.common.descriptor.dao;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoDescriptorDaoTest {
    @Autowired
    private MongoDescriptorFactory mongoFactory;

    @Autowired
    private DescriptorDao descriptorDao;

    @Autowired
    private Datastore descriptorsStore;

    @Test
    public void testRetrieveByExternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    public void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(objectId.toString());
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    public void testRetrieveMapByExternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoFactory.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByExternalIds(Collections.singleton(externalId));
        assertEquals(1, retrievedMap.size());
        assertEquals(objectId.toString(), retrievedMap.get(externalId));
    }

    @Test
    public void testRetrieveMapByInternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoFactory.create(objectId, "test_model");

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
}
