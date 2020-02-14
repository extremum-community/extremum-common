package integration.web;

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

import java.nio.file.Paths;

@MockBeans({
        @MockBean(RoleSecurity.class),
        @MockBean(DataSecurity.class),
        @MockBean(PrincipalSource.class),
        @MockBean(SecurityProvider.class)
})
@Configuration
@EnableAutoConfiguration(exclude = MongoReactiveDataAutoConfiguration.class)
public class WebTestConfiguration {
    @Bean
    public NetworkntSchemaProvider networkntSchemaProvider() {
        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        return new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas")
        );
    }
}
