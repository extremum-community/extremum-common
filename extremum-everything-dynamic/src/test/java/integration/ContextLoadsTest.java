package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.starter.CommonConfiguration;
import io.extremum.test.containers.CoreServices;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("load-context-test")
@ContextConfiguration(classes = {CommonConfiguration.class, DynamicModuleAutoConfiguration.class})
@SpringBootTest
public class ContextLoadsTest extends CoreServices {
    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Test
    void contextLoads() {

    }
}
