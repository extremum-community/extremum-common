package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ActiveProfiles;

@MockBeans({
        @MockBean(MongoDynamicModelDao.class)
})
@ActiveProfiles("load-context-test")
@SpringBootTest(classes = {DynamicModuleAutoConfiguration.class})
public class ContextLoadsTest {
    @Test
    void contextLoads() {

    }
}
