package descriptor.config;

import com.extremum.common.collection.dao.BaseCollectionDescriptorDaoImpl;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.mapper.JsonObjectMapper;
import config.AppConfiguration;
import config.DescriptorsProperties;
import config.RedisProperties;
import org.mongodb.morphia.Datastore;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;


@Configuration
@Import(AppConfiguration.class)
@EnableConfigurationProperties({RedisProperties.class, DescriptorsProperties.class})
public class DescriptorConfiguration {
    @Autowired
    private RedisProperties redisProps;
    @Autowired
    private DescriptorsProperties descriptorsProperties;

    @Bean
    @DependsOn("redisContainer")
    public RedissonClient redissonClient() {
        Config config =  new Config();
        config.useSingleServer().setAddress(redisProps.getUri());
        return Redisson.create(config);
    }

    @Bean(name="redisContainer")
    public GenericContainer redisContainer() {
        GenericContainer redis = new GenericContainer("redis:5.0.4").withExposedPorts(6379);
        redis.start();
        redisProps.setUri("redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort());
        return redis;
    }

    @Bean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        Codec codec=new TypedJsonJacksonCodec(String.class,Descriptor.class, JsonObjectMapper.createdWithoutDescriptorTransfiguration());
        return new BaseDescriptorDaoImpl(redissonClient, descriptorsStore, descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(),codec, redisProps.getCacheSize(), redisProps.getIdleTime());
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
}
