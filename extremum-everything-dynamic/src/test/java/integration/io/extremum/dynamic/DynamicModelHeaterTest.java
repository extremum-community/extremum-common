package integration.io.extremum.dynamic;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

@MockBeans({
        @MockBean(DefaultJsonDynamicModelMetadataProvider.class)
})
@ActiveProfiles({"load-context-test"})
@SpringBootTest(classes = {DynamicModelHeaterTestConfiguration.class})
public class DynamicModelHeaterTest extends SpringBootTestWithServices {
    @Autowired
    ReactiveDescriptorDeterminator descriptorDeterminator;

    @Test
    void heaterLoadSchemaAndRegisterModelInADescriptorDeterminator() {
        Set<String> registeredModelNames = descriptorDeterminator.getRegisteredModelNames();

        Assertions.assertEquals(1, registeredModelNames.size());
        Assertions.assertTrue(registeredModelNames.contains("DynamicModelName"));
    }
}
