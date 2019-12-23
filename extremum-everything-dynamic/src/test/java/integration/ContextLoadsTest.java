package integration;

import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("load-context-test")
public class ContextLoadsTest extends SpringBootTestWithServices {
    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Test
    void contextLoads() {

    }
}
