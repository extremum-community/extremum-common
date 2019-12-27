package integration.io.extremum.dynamic;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.starter.CommonConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.doReturn;

@Import({CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        DynamicModuleAutoConfiguration.class})
@Configuration
public class DynamicModelHeaterTestConfiguration {
    @Bean
    @Primary
    public GithubNetworkntSchemaProvider githubNetworkntSchemaProvider() throws URISyntaxException {
        GithubNetworkntSchemaProvider provider = Mockito.mock(GithubNetworkntSchemaProvider.class);

        Path schemaDirectory = Paths.get(this.getClass().getClassLoader().getResource("test.file.txt").toURI());

        FileSystemNetworkntSchemaProvider fileSystemNetworkntSchemaProvider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(schemaDirectory.getParent().toString(), "schemas")
        );

        doReturn(fileSystemNetworkntSchemaProvider.loadSchema("complex.schema.json"))
                .when(provider).loadSchema("main.schema.json");

        return provider;
    }
}
