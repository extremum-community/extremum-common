package io.extremum.mongo.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.extremum.common.annotation.SecondaryDatasource;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.MongoCommonModelLifecycleCallbacks;
import io.extremum.mongo.springdata.EnableAllMongoAuditing;
import io.extremum.mongo.springdata.MainMongoDb;
import io.extremum.starter.DateToZonedDateTimeConverter;
import io.extremum.starter.DescriptorToStringConverter;
import io.extremum.starter.ZonedDateTimeToDateConverter;
import lombok.RequiredArgsConstructor;
import org.bson.UuidRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

/**
 * @author rpuch
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MongoProperties.class)
@EnableAllMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class MainMongoConfiguration extends AbstractMongoClientConfiguration {
    private final MongoProperties mongoProperties;
    private final List<CustomMongoConvertersSupplier> customMongoConvertersSuppliers;

    @Override
    @Bean
    @MainMongoDb
    public MongoDatabaseFactory mongoDbFactory() {
        return super.mongoDbFactory();
    }

    @Override
    @Bean
    @Primary
    @MainMongoDb
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
        MongoTemplate template = super.mongoTemplate(databaseFactory, converter);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Override
    @Bean
    @MainMongoDb
    public MongoClient mongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getServiceDbName();
    }

    @Override
    @Bean
    @Primary
    @MainMongoDb
    public MongoMappingContext mongoMappingContext(MongoCustomConversions customConversions)
            throws ClassNotFoundException {
        return super.mongoMappingContext(customConversions);
    }

    @Override
    @Bean
    @Primary
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory,
    			MongoCustomConversions customConversions, MongoMappingContext mappingContext) {
        MappingMongoConverter converter = super.mappingMongoConverter(databaseFactory, customConversions,
                mappingContext);

        // changing type mapper so that:
        // 1. if there is no @TypeAlias on the model class, _class attribute is not saved
        // 2. if @TypeAlias is there, its value is saved in _class attribute
        MappingContextTypeInformationMapper typeInformationMapper = new MappingContextTypeInformationMapper(
                mappingContext);
        DefaultMongoTypeMapper typeMapper = new MongoTypeMapperWithSearchByExampleFix(typeInformationMapper);
        converter.setTypeMapper(typeMapper);

        return converter;
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return mongoProperties.getModelPackages();
    }

    @Override
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        return super.getInitialEntitySet().stream()
                .filter(this::isNotOnSecondaryDatasource)
                .collect(toSet());
    }

    private boolean isNotOnSecondaryDatasource(Class<?> entityClass) {
        return AnnotationUtils.findAnnotation(entityClass, SecondaryDatasource.class) == null;
    }

    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();

        converters.add(new DateToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateConverter());
        converters.add(new DescriptorToStringConverter());
        converters.add(new EnumToStringConverter());
        converters.add(new StringToEnumConverterFactory());

        customMongoConvertersSuppliers.forEach(supplier -> {
            converters.addAll(supplier.getConverters());
        });

        return new MongoCustomConversions(converters);
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDescriptorFacilities mongoDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver) {
        return new MongoDescriptorFacilitiesImpl(descriptorFactory, descriptorSaver);
    }

    @Bean
    public MongoCommonModelLifecycleCallbacks mongoCommonModelLifecycleCallbacks(
            MongoDescriptorFacilities mongoDescriptorFacilities) {
        return new MongoCommonModelLifecycleCallbacks(mongoDescriptorFacilities);
    }

}
