package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("load-context-test")
@SpringBootTest(classes = {DynamicModuleAutoConfiguration.class})
public class ContextLoadsTest {
    @Test
    void contextLoads() {

    }
}
