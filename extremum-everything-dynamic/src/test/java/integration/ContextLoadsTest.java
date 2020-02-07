package integration;

import io.extremum.dynamic.metadata.impl.DefaultDynamicModelMetadataProviderService;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("load-context-test")
@ContextConfiguration(classes = ContextLoadsTestConfiguration.class)
public class ContextLoadsTest extends SpringBootTestWithServices {
    @MockBean
    DefaultDynamicModelMetadataProviderService metadataProvider;

    @MockBean
    RoleSecurity roleSecurity;

    @MockBean
    DataSecurity dataSecurity;

    @MockBean
    PrincipalSource principalSource;

    @Test
    void contextLoads() {

    }
}
