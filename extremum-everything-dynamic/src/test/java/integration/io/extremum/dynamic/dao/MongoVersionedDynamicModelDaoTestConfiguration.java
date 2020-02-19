package integration.io.extremum.dynamic.dao;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.dynamic.dao.JsonDynamicModelDao;
import io.extremum.dynamic.dao.MongoVersionedDynamicModelDao;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.security.DataSecurity;
import io.extremum.security.RoleSecurity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

@MockBeans({
        @MockBean(DataSecurity.class),
        @MockBean(RoleSecurity.class),
        @MockBean(SecurityProvider.class)
})
@EnableAutoConfiguration
public class MongoVersionedDynamicModelDaoTestConfiguration {
    @Bean
    public JsonDynamicModelDao jsonDynamicModelDao(ReactiveMongoOperations ops, ReactiveMongoDescriptorFacilities facilities) {
        return new MongoVersionedDynamicModelDao(ops, facilities);
    }
}
