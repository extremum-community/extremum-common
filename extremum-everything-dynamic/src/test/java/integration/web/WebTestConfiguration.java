package integration.web;

import configurations.FileSystemSchemaProviderConfiguration;
import io.extremum.authentication.api.SecurityProvider;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@MockBeans({
        @MockBean(RoleSecurity.class),
        @MockBean(DataSecurity.class),
        @MockBean(PrincipalSource.class),
        @MockBean(SecurityProvider.class)
})
@Configuration
@Import(FileSystemSchemaProviderConfiguration.class)
@EnableAutoConfiguration(exclude = MongoReactiveDataAutoConfiguration.class)
public class WebTestConfiguration {
}
