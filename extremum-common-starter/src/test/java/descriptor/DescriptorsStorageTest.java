package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.test.TestWithServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorsStorageTest extends TestWithServices {
    @Autowired
    @Qualifier("descriptorsMongoTemplate")
    private MongoOperations mongoOperations;

    @Test
    void noCollectionDescriptorCollectionShouldBeCreated() {
        assertFalse(mongoOperations.collectionExists("collectionDescriptor"));
    }
}
