package io.extremum.mongo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import io.extremum.common.annotation.SecondaryDatasource;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorLifecycleListener;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.MongoCommonModelLifecycleListener;
import io.extremum.mongo.springdata.EnableAllMongoAuditing;
import io.extremum.mongo.springdata.MainMongoDb;
import io.extremum.starter.DateToZonedDateTimeConverter;
import io.extremum.starter.DescriptorToStringConverter;
import io.extremum.starter.ZonedDateTimeToDateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableAllMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class MainMongoConfiguration extends AbstractMongoConfiguration {
    private final MongoProperties mongoProperties;
    private final List<CustomMongoConvertersSupplier> customMongoConvertersSuppliers;

    @Bean
    public MongoClientURI mongoDatabaseUri() {
        return new MongoClientURI(mongoProperties.getUri());
    }

    @Override
    @Bean
    @MainMongoDb
    public MongoDbFactory mongoDbFactory() {
        return super.mongoDbFactory();
    }

    @Override
    @Bean
    @Primary
    @MainMongoDb
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate template = super.mongoTemplate();
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Override
    @Bean
    @MainMongoDb
    public MongoClient mongoClient() {
        return new MongoClient(mongoDatabaseUri());
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getServiceDbName();
    }

    @Override
    @Bean
    @Primary
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        MappingMongoConverter converter = super.mappingMongoConverter();

        // changing type mapper so that:
        // 1. if there is no @TypeAlias on the model class, _class attribute is not saved
        // 2. if @TypeAlias is there, its value is saved in _class attribute
        MappingContextTypeInformationMapper typeInformationMapper = new MappingContextTypeInformationMapper(
                mongoMappingContext());
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
    public DescriptorLifecycleListener descriptorLifecycleListener() {
        return new DescriptorLifecycleListener();
    }

}
