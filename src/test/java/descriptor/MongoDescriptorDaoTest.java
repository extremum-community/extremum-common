package descriptor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.stucts.*;
import com.extremum.common.test.TestWithServices;
import config.DescriptorConfiguration;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DescriptorConfiguration.class)
public class MongoDescriptorDaoTest extends TestWithServices {
    @Autowired
    private DescriptorDao descriptorDao;
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Test
    public void testRetrieveByExternalId() {
        Descriptor descriptor = createADescriptor();

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    private Descriptor createADescriptor() {
        ObjectId objectId = new ObjectId();
        return MongoDescriptorFactory.create(objectId, "test_model");
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

        descriptorRepository.save(descriptor);
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
    public void testSaveDisplayFieldAsSerializedString() {
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

    @Test
    public void givenADescriptorExists_whenItIsSearchedFor_thenItShouldBeFound() {
        Descriptor descriptor = createADescriptor();

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(true));
    }

    @Test
    public void givenADescriptorIsSoftDeleted_whenItIsSearchedFor_thenItShouldNotBeFound() {
        Descriptor descriptor = createADescriptor();

        descriptor.setDeleted(true);
        descriptorRepository.save(descriptor);

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(false));
    }
}
