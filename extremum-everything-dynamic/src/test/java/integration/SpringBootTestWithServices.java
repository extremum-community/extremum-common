package integration;

import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.starter.CommonConfiguration;
import io.extremum.test.containers.CoreServices;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {CommonConfiguration.class, DynamicModuleAutoConfiguration.class})
@SpringBootTest
public class SpringBootTestWithServices extends CoreServices {

}
