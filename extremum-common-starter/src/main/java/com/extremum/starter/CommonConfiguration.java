package com.extremum.starter;

import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.descriptor.service.DescriptorServiceConfigurator;
import com.extremum.common.descriptor.service.DescriptorServiceImpl;
import com.extremum.common.mapper.BasicJsonObjectMapper;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.MapperDependenciesImpl;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.service.lifecycle.MongoCommonModelLifecycleListener;
import com.extremum.starter.properties.DescriptorsProperties;
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
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Import({DescriptorMongoConfiguration.class, MongoRepositoriesConfiguration.class})
@RequiredArgsConstructor
@ComponentScan("com.extremum.common.dto.converters")
@EnableConfigurationProperties({RedisProperties.class, MongoProperties.class,
        DescriptorsProperties.class})
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class CommonConfiguration {
    private final RedisProperties redisProperties;
    private final MongoProperties mongoProperties;
    private final DescriptorsProperties descriptorsProperties;

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new AuditingDateTimeProvider();
    }

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
                new BasicJsonObjectMapper()));
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
    public DescriptorService descriptorService(DescriptorDao descriptorDao) {
        return new DescriptorServiceImpl(descriptorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorServiceConfigurator configurator(DescriptorService descriptorService) {
        return new DescriptorServiceConfigurator(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionDescriptorDao collectionDescriptorDao(RedissonClient redissonClient,
                                                           CollectionDescriptorRepository collectionDescriptorRepository) {
        return CollectionDescriptorDaoFactory.create(redisProperties, descriptorsProperties, redissonClient,
                collectionDescriptorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionDescriptorService collectionDescriptorService(CollectionDescriptorDao collectionDescriptorDao) {
        return new CollectionDescriptorServiceImpl(collectionDescriptorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperDependencies mapperDependencies(CollectionDescriptorService collectionDescriptorService) {
        return new MapperDependenciesImpl(collectionDescriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper jacksonObjectMapper(MapperDependencies mapperDependencies) {
        return new SystemJsonObjectMapper(mapperDependencies);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorFactory descriptorFactory(DescriptorService descriptorService) {
        return new DescriptorFactory(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDescriptorFactory mongoDescriptorFactory(DescriptorFactory descriptorFactory) {
        return new MongoDescriptorFactory(descriptorFactory);
    }

    @Bean
    public MongoCommonModelLifecycleListener mongoCommonModelLifecycleListener(
            MongoDescriptorFactory mongoDescriptorFactory) {
        return new MongoCommonModelLifecycleListener(mongoDescriptorFactory);
    }
}
