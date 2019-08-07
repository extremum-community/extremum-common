package com.extremum.starter;

import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFacilitiesImpl;
import com.extremum.common.descriptor.service.DBDescriptorLoader;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.descriptor.service.DescriptorServiceImpl;
import com.extremum.common.descriptor.service.StaticDescriptorLoaderAccessorConfigurator;
import com.extremum.common.mapper.BasicJsonObjectMapper;
import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.MapperDependenciesImpl;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.service.lifecycle.MongoCommonModelLifecycleListener;
import com.extremum.common.uuid.StandardUUIDGenerator;
import com.extremum.common.uuid.UUIDGenerator;
import com.extremum.sharedmodels.descriptor.DescriptorLoader;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

@Configuration
@Import({MainMongoConfiguration.class, DescriptorsMongoConfiguration.class, MongoRepositoriesConfiguration.class})
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
    public RedissonClient redissonClient(@Qualifier("redis") ObjectMapper redisMapper) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec(redisMapper));
        config.useSingleServer().setAddress(redisProperties.getUri());
        if (redisProperties.getPassword() != null) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedisConnectionFactory redisConnectionFactory(RedissonClient client) {
        return new RedissonConnectionFactory(client);
    }

    @Bean
    public MongoClientURI mongoDatabaseUri() {
        return new MongoClientURI(mongoProperties.getMainUri());
    }

    @Bean
    public UUIDGenerator uuidGenerator() {
        return new StandardUUIDGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, DescriptorRepository descriptorRepository) {
        return DescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorService descriptorService(DescriptorDao descriptorDao, UUIDGenerator uuidGenerator) {
        return new DescriptorServiceImpl(descriptorDao, uuidGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorLoader descriptorLoader(DescriptorService descriptorService) {
        return new DBDescriptorLoader(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public StaticDescriptorLoaderAccessorConfigurator staticDescriptorLoaderAccessorConfigurator(
            DescriptorLoader descriptorLoader) {
        return new StaticDescriptorLoaderAccessorConfigurator(descriptorLoader);
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
    public MapperDependencies mapperDependencies(DescriptorFactory descriptorFactory,
                                                 CollectionDescriptorService collectionDescriptorService) {
        return new MapperDependenciesImpl(descriptorFactory, collectionDescriptorService);
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(MapperDependencies mapperDependencies,
                                            List<SystemMapperModulesSupplier> systemModulesSuppliers) {
        SystemJsonObjectMapper objectMapper = new SystemJsonObjectMapper(mapperDependencies);
        systemModulesSuppliers.forEach(supplier -> objectMapper.registerModules(supplier.makeModules(objectMapper)));
        return objectMapper;
    }

    @Bean
    @Qualifier("redis")
    public ObjectMapper redisObjectMapper(List<RedisMapperModulesSupplier> redisModuleSuppliers) {
        BasicJsonObjectMapper objectMapper = new BasicJsonObjectMapper();
        redisModuleSuppliers.forEach(supplier -> objectMapper.registerModules(supplier.makeModules(objectMapper)));
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DescriptorSaver descriptorSaver(DescriptorService descriptorService) {
        return new DescriptorSaver(descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDescriptorFacilities mongoDescriptorFacilities(DescriptorFactory descriptorFactory,
                                                               DescriptorSaver descriptorSaver) {
        return new MongoDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver);
    }

    @Bean
    public MongoCommonModelLifecycleListener mongoCommonModelLifecycleListener(
            MongoDescriptorFacilities mongoDescriptorFacilities) {
        return new MongoCommonModelLifecycleListener(mongoDescriptorFacilities);
    }
}
