package com.extremum.starter;

import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.BaseCollectionDescriptorDaoImpl;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.service.DescriptorServiceConfigurator;
import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.MapperDependenciesImpl;
import com.extremum.common.service.MongoCommonModelLifecycleListener;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.ElasticProperties;
import com.extremum.starter.properties.MongoProperties;
import com.extremum.starter.properties.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Import(DescriptorMongoConfiguration.class)
@RequiredArgsConstructor
@ComponentScan("com.extremum.common.dto.converters")
@EnableConfigurationProperties({RedisProperties.class, MongoProperties.class, ElasticProperties.class,
        DescriptorsProperties.class})
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

        config.setCodec(new JsonJacksonCodec(
                JsonObjectMapper.createWithoutDescriptorTransfiguration()));
        config.useSingleServer().setAddress(redisProperties.getUri());
        if (redisProperties.getPassword() != null) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
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
    public DescriptorDao descriptorDao(RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Descriptor.class,
                JsonObjectMapper.createWithoutDescriptorTransfiguration());
        if (noRedis()) {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository,
                    descriptorsProperties.getDescriptorsMapName(), descriptorsProperties.getInternalIdsMapName(), codec);
        } else {
            return new BaseDescriptorDaoImpl(redissonClient, descriptorRepository, descriptorsProperties.getDescriptorsMapName(),
                    descriptorsProperties.getInternalIdsMapName(), codec, redisProperties.getCacheSize(),
                    redisProperties.getIdleTime());
        }
    }

    private boolean noRedis() {
        return redisProperties.getCacheSize() == 0 && redisProperties.getIdleTime() == 0;
    }

    @Bean
    @ConditionalOnBean(DescriptorDao.class)
    @ConditionalOnMissingBean
    public DescriptorServiceConfigurator configurator(DescriptorDao descriptorDao) {
        return new DescriptorServiceConfigurator(descriptorDao);
    }

    @Bean
    @ConditionalOnProperty(prefix = "descriptors", value = {"collectionDescriptorsMapName", "collectionCoordinatesMapName"})
    @ConditionalOnMissingBean
    public CollectionDescriptorDao collectionDescriptorDao(RedissonClient redissonClient,
            CollectionDescriptorRepository collectionDescriptorRepository) {
        if (noRedis()) {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, collectionDescriptorRepository,
                    descriptorsProperties.getCollectionDescriptorsMapName(),
                    descriptorsProperties.getCollectionCoordinatesMapName());
        } else {
            return new BaseCollectionDescriptorDaoImpl(redissonClient, collectionDescriptorRepository,
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
    @ConditionalOnBean(CollectionDescriptorService.class)
    @ConditionalOnMissingBean
    public MapperDependencies mapperDependencies(CollectionDescriptorService collectionDescriptorService) {
        return new MapperDependenciesImpl(collectionDescriptorService);
    }

    @Bean
    @ConditionalOnBean(MapperDependencies.class)
    @Primary
    public ObjectMapper jacksonObjectMapper(MapperDependencies mapperDependencies) {
        return JsonObjectMapper.createWithCollectionDescriptors(mapperDependencies);
    }

    @Bean
    @ConditionalOnMissingBean(MapperDependencies.class)
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return JsonObjectMapper.createWithDescriptors();
    }

    @Bean
    public MongoCommonModelLifecycleListener mongoCommonModelLifecycleListener() {
        return new MongoCommonModelLifecycleListener();
    }
}
