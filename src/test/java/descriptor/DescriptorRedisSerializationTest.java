package descriptor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.test.TestWithServices;
import com.extremum.starter.DescriptorDaoFactory;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.RedisProperties;
import config.DescriptorConfiguration;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorRedisSerializationTest extends TestWithServices {
    @Autowired
    private DescriptorRepository descriptorRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;

    private DescriptorDao freshDaoToAvoidCachingInMemory;

    @BeforeEach
    void init() {
        freshDaoToAvoidCachingInMemory = DescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository);
    }

    @Test
    void whenLoadingADescriptorFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Descriptor descriptor = createADescriptor();

        Optional<Descriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByExternalId(
                descriptor.getExternalId());
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    private Descriptor createADescriptor() {
        ObjectId objectId = new ObjectId();
        return MongoDescriptorFactory.create(objectId, "test_model");
    }

}
