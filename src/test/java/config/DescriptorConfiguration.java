package config;

import com.extremum.common.collection.dao.BaseCollectionDescriptorDaoImpl;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.config.DescriptorDatastoreFactory;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.descriptor.service.DescriptorServiceConfigurator;
import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.MongoProperties;
import com.extremum.starter.properties.RedisProperties;
import common.dao.TestModelDao;
import lombok.RequiredArgsConstructor;
import org.mongodb.morphia.Datastore;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.testcontainers.containers.GenericContainer;


@Configuration
@EnableConfigurationProperties({MongoProperties.class, RedisProperties.class, DescriptorsProperties.class})
@RequiredArgsConstructor
public class DescriptorConfiguration {
    private final MongoProperties mongoProps;
    private final RedisProperties redisProps;
    private final DescriptorsProperties descriptorsProperties;

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

    @Bean
    @DependsOn("redisContainer")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(redisProps.getUri());
        return Redisson.create(config);
    }

    @Bean(name = "redisContainer")
    public GenericContainer redisContainer() {
        GenericContainer redis = new GenericContainer("redis:5.0.4").withExposedPorts(6379);
        redis.start();
        redisProps.setUri("redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort());
        return redis;
    }

    @Bean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Descriptor.class,
                JsonObjectMapper.createWithoutDescriptorTransfiguration());
        return new BaseDescriptorDaoImpl(redissonClient, descriptorsStore, descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(), codec, redisProps.getCacheSize(), redisProps.getIdleTime());
    }

    @Bean
    public CollectionDescriptorDao collectionDescriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        return new BaseCollectionDescriptorDaoImpl(redissonClient, descriptorsStore,
                descriptorsProperties.getCollectionDescriptorsMapName(),
                descriptorsProperties.getCollectionCoordinatesMapName(),
                redisProps.getCacheSize(), redisProps.getIdleTime());
    }

    @Bean
    public CollectionDescriptorService collectionDescriptorService(CollectionDescriptorDao collectionDescriptorDao) {
        return new CollectionDescriptorServiceImpl(collectionDescriptorDao);
    }

    @Bean
    public DescriptorServiceConfigurator configurator(DescriptorDao descriptorDao){
        return new DescriptorServiceConfigurator(descriptorDao);
    }
}
