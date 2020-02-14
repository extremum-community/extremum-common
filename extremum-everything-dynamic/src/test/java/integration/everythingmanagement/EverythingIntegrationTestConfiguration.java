package integration.everythingmanagement;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@MockBeans({
        @MockBean(DataSecurity.class),
        @MockBean(RoleSecurity.class),
        @MockBean(PrincipalSource.class),
        @MockBean(SecurityProvider.class)
})
@Configuration
@EnableAutoConfiguration(exclude = MongoReactiveDataAutoConfiguration.class)
public class EverythingIntegrationTestConfiguration {
    @Bean
    public NetworkntSchemaProvider networkntSchemaProvider() throws URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("test.file.txt");
        Path directory = Paths.get(Paths.get(url.toURI()).getParent().toString(), "/schemas/");

        return new FileSystemNetworkntSchemaProvider(JsonSchemaType.V2019_09, directory);
    }
}
