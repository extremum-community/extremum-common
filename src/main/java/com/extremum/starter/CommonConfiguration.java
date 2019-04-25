package com.extremum.starter;

import com.extremum.common.collection.dao.BaseCollectionDescriptorDaoImpl;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.config.DescriptorDatastoreFactory;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.descriptor.service.DescriptorServiceConfigurator;
import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.MongoProperties;
import com.extremum.starter.properties.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mongodb.morphia.Datastore;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({RedisProperties.class, MongoProperties.class, DescriptorsProperties.class})
public class CommonConfiguration {
    private final RedisProperties redisProperties;
    private final MongoProperties mongoProperties;
    private final DescriptorsProperties descriptorsProperties;

    @Bean
    @ConditionalOnProperty(value = "redis.uri")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() {
        InputStream redisStream = CommonConfiguration.class.getClassLoader().getResourceAsStream("redis.json");
        Config config;
        try {
            config = Config.fromJSON(redisStream);
        } catch (IOException e) {
            config = new Config();
        }
        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec((Class<?>) null,
                JsonObjectMapper.createdWithoutDescriptorTransfiguration());

        config.setCodec(codec);
        config.useSingleServer().setAddress(redisProperties.getUri());
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(prefix = "mongo", value = {"uri", "dbName"})
    @ConditionalOnMissingBean
    public Datastore descriptorsStore() {
        return new DescriptorDatastoreFactory().createDescriptorDatastore(mongoProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedisConnectionFactory redisConnectionFactory() {
        return new RedissonConnectionFactory(redissonClient());
    }

    @Bean
    @ConditionalOnProperty(prefix = "descriptors", value = {"descriptorsMapName", "internalIdsMapName"})
    @ConditionalOnMissingBean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        if (noRedis()) {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorsStore, descriptorsProperties.getDescriptorsMapName(), descriptorsProperties.getInternalIdsMapName());
        } else {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorsStore, descriptorsProperties.getDescriptorsMapName(),
                    descriptorsProperties.getInternalIdsMapName(), redisProperties.getCacheSize(), redisProperties.getIdleTime());
        }
    }

    private boolean noRedis() {
        return redisProperties.getCacheSize() == 0 && redisProperties.getIdleTime() == 0;
    }

    @Bean
    @ConditionalOnBean(DescriptorDao.class)
    @ConditionalOnMissingBean
    public DescriptorServiceConfigurator configurator(DescriptorDao descriptorDao) {
        DescriptorServiceConfigurator descriptorServiceConfigurator = new DescriptorServiceConfigurator(descriptorDao);
        descriptorServiceConfigurator.init();
        return descriptorServiceConfigurator;
    }

    @Bean
    @ConditionalOnProperty(prefix = "descriptors", value = {"collectionDescriptorsMapName", "collectionCoordinatesMapName"})
    @ConditionalOnMissingBean
    public CollectionDescriptorDao collectionDescriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        if (noRedis()) {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, descriptorsStore,
                    descriptorsProperties.getCollectionDescriptorsMapName(),
                    descriptorsProperties.getCollectionCoordinatesMapName());
        } else {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, descriptorsStore,
                    descriptorsProperties.getCollectionDescriptorsMapName(),
                    descriptorsProperties.getCollectionCoordinatesMapName(),
                    redisProperties.getCacheSize(), redisProperties.getIdleTime());
        }
    }

    @Bean
    @ConditionalOnBean(CollectionDescriptorDao.class)
    @ConditionalOnMissingBean
    public CollectionDescriptorService collectionDescriptorService(CollectionDescriptorDao collectionDescriptorDao) {
        return new CollectionDescriptorServiceImpl(collectionDescriptorDao);
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return JsonObjectMapper.createdMapper();
    }
}
