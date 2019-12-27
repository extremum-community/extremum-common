package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.everything.management.HybridEverythingManagementService;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.starter.CommonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MockBeans({
        @MockBean(DefaultJsonDynamicModelMetadataProvider.class)
})
@ActiveProfiles("load-context-test")
@SpringBootTest(classes = {
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
public class ReplaceReactiveEverythingManagementServiceBeanPostProcessorTest extends SpringBootTestWithServices {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    void reactiveEverythingManagementService_impl_replacedToHybridEverythingManagementService_after_contextLoads() {
        ReactiveEverythingManagementService service = applicationContext.getBean(ReactiveEverythingManagementService.class);

        assertNotNull(service);
        assertTrue(HybridEverythingManagementService.class.isAssignableFrom(service.getClass()),
                format("Bean of type %s are expected in application context, but context contains bean of type %s",
                        HybridEverythingManagementService.class, service.getClass()));
    }
}
