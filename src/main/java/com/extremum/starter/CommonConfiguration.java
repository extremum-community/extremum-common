package com.extremum.starter;

import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.service.DescriptorServiceConfigurator;
import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.MapperDependenciesImpl;
import com.extremum.common.service.lifecycle.MongoCommonModelLifecycleListener;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.ElasticProperties;
import com.extremum.starter.properties.MongoProperties;
import com.extremum.starter.properties.RedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientURI;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Configuration
@Import({DescriptorMongoConfiguration.class, MongoRepositoriesConfiguration.class, JpaRepositoriesConfiguration.class})
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
    public MongoClientURI mongoDatabaseUri() {
        return new MongoClientURI(mongoProperties.getUri());
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        return DescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorServiceConfigurator configurator(DescriptorDao descriptorDao) {
        return new DescriptorServiceConfigurator(descriptorDao);
    }

    @Bean
    @CollectionDescriptorsEnabledCondition
    @ConditionalOnMissingBean
    public CollectionDescriptorDao collectionDescriptorDao(RedissonClient redissonClient,
                                                           CollectionDescriptorRepository collectionDescriptorRepository) {
        return CollectionDescriptorDaoFactory.create(redisProperties, descriptorsProperties, redissonClient,
                collectionDescriptorRepository);
    }

    @Bean
    @CollectionDescriptorsEnabledCondition
    @ConditionalOnMissingBean
    public CollectionDescriptorService collectionDescriptorService(CollectionDescriptorDao collectionDescriptorDao) {
        return new CollectionDescriptorServiceImpl(collectionDescriptorDao);
    }

    @Bean
    @CollectionDescriptorsEnabledCondition
    @ConditionalOnMissingBean
    public MapperDependencies mapperDependencies(CollectionDescriptorService collectionDescriptorService) {
        return new MapperDependenciesImpl(collectionDescriptorService);
    }

    @Bean
    @CollectionDescriptorsEnabledCondition
    @Primary
    public ObjectMapper jacksonObjectMapper(MapperDependencies mapperDependencies) {
        return JsonObjectMapper.createWithCollectionDescriptors(mapperDependencies);
    }

    @Bean
    @Conditional(CollectionDescriptorsDisabledCondition.class)
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return JsonObjectMapper.createWithDescriptors();
    }

    @Bean
    public MongoCommonModelLifecycleListener mongoCommonModelLifecycleListener() {
        return new MongoCommonModelLifecycleListener();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @ConditionalOnProperty(prefix = "descriptors", value = {"collectionDescriptorsMapName", "collectionCoordinatesMapName"})
    private @interface CollectionDescriptorsEnabledCondition {
    }

    private static class CollectionDescriptorsDisabledCondition extends NoneNestedConditions {

        public CollectionDescriptorsDisabledCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @CollectionDescriptorsEnabledCondition
        private class CollectionDescriptorsEnabled {

        }
    }
}
