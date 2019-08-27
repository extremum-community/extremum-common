package io.extremum.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import io.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.CollectionDescriptorServiceImpl;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorServiceImpl;
import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import io.extremum.common.descriptor.factory.impl.MongoDescriptorFacilitiesImpl;
import io.extremum.common.descriptor.service.*;
import io.extremum.common.mapper.BasicJsonObjectMapper;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.MapperDependenciesImpl;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.models.Model;
import io.extremum.common.mongo.MongoUniversalReactiveModelLoader;
import io.extremum.common.reactive.IsolatedSchedulerReactifier;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.service.CommonService;
import io.extremum.common.service.lifecycle.MongoCommonModelLifecycleListener;
import io.extremum.common.support.*;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.ModelProperties;
import io.extremum.starter.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
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
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Configuration
@Import({MainMongoConfiguration.class, MainReactiveMongoConfiguration.class,
        DescriptorsMongoConfiguration.class, MongoRepositoriesConfiguration.class})
@RequiredArgsConstructor
@ComponentScan("io.extremum.common.dto.converters")
@EnableConfigurationProperties({RedisProperties.class, DescriptorsProperties.class, ModelProperties.class})
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class CommonConfiguration {
    private final RedisProperties redisProperties;
    private final DescriptorsProperties descriptorsProperties;
    private final ModelProperties modelProperties;

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new AuditingDateTimeProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public Config redissonConfig(@Qualifier("redis") ObjectMapper redisMapper) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec(redisMapper));
        config.useSingleServer().setAddress(redisProperties.getUri());
        if (redisProperties.getPassword() != null) {
            config.useSingleServer().setPassword(redisProperties.getPassword());
        }
        return config;
    }

    @Bean
    @ConditionalOnProperty(value = "redis.uri")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(Config redissonConfig) {
        return Redisson.create(redissonConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedisConnectionFactory redisConnectionFactory(RedissonClient client) {
        return new RedissonConnectionFactory(client);
    }

    @Bean
    @ConditionalOnProperty(value = "redis.uri")
    @ConditionalOnMissingBean
    public RedissonReactiveClient redissonReactiveClient(Config redissonConfig) {
        return Redisson.createReactive(redissonConfig);
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
    public ReactiveDescriptorDao reactiveDescriptorDao(RedissonReactiveClient redissonReactiveClient,
                                                       DescriptorRepository descriptorRepository) {
        return DescriptorDaoFactory.createReactive(redisProperties, descriptorsProperties,
                redissonReactiveClient, descriptorRepository);
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
    public ReactiveDescriptorService reactiveDescriptorService(ReactiveDescriptorDao reactiveDescriptorDao) {
        return new ReactiveDescriptorServiceImpl(reactiveDescriptorDao);
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
    public ReactiveCollectionDescriptorDao reactiveCollectionDescriptorDao(
            RedissonReactiveClient redissonReactiveClient,
            CollectionDescriptorRepository collectionDescriptorRepository) {
        return CollectionDescriptorDaoFactory.createReactive(redisProperties, descriptorsProperties,
                redissonReactiveClient, collectionDescriptorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionDescriptorService collectionDescriptorService(DescriptorService descriptorService,
                                                                   DescriptorDao descriptorDao) {
        return new CollectionDescriptorServiceImpl(descriptorService, descriptorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCollectionDescriptorService reactiveCollectionDescriptorService(
            ReactiveCollectionDescriptorDao reactiveCollectionDescriptorDao) {
        return new ReactiveCollectionDescriptorServiceImpl(reactiveCollectionDescriptorDao);
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

    @Bean
    @ConditionalOnMissingBean
    public CommonServices commonServices(List<CommonService<? extends Model>> services) {
        return new ListBasedCommonServices(services);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelClasses modelClasses() {
        return new ScanningModelClasses(modelProperties.getPackageNames());
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalModelFinder universalModelFinder(ModelClasses modelClasses, CommonServices commonServices) {
        return new UniversalModelFinderImpl(modelClasses, commonServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoUniversalReactiveModelLoader mongoUniversalReactiveModelLoader(
            ReactiveMongoOperations reactiveMongoOperations) {
        return new MongoUniversalReactiveModelLoader(reactiveMongoOperations);
    }

    @Bean(destroyMethod = "dispose")
    public Scheduler reactifierScheduler() {
        // per https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking
        return Schedulers.elastic();
    }

    @Bean
    @ConditionalOnMissingBean
    public Reactifier reactifier(@Qualifier("reactifierScheduler") Scheduler reactifierScheduler) {
        return new IsolatedSchedulerReactifier(reactifierScheduler);
    }
}
