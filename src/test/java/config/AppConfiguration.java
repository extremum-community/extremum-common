package config;

import com.extremum.common.descriptor.config.DescriptorDatastoreFactory;
import com.extremum.starter.properties.MongoProperties;
import common.dao.TestModelDao;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.testcontainers.containers.GenericContainer;


@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class AppConfiguration {
    @Autowired
    private MongoProperties mongoProps;

    @Bean
    @DependsOn("mongoContainer")
    public Datastore datastore() {
        return new DescriptorDatastoreFactory().createDescriptorDatastore(mongoProps);
    }

    @Bean
    public TestModelDao testModelDao(Datastore datastore) {
        return new TestModelDao(datastore);
    }

    @Bean(name = "mongoContainer")
    public GenericContainer mongoContainer() {
        GenericContainer mongo = new GenericContainer("mongo:3.4-xenial").withExposedPorts(27017);
        mongo.start();
        mongoProps.setUri("mongodb://" + mongo.getContainerIpAddress() + ":" + mongo.getFirstMappedPort());
        return mongo;
    }
}
