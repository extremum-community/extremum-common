package io.extremum.mongo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorLifecycleListener;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.facilities.MongoDescriptorFacilitiesImpl;
import io.extremum.mongo.service.lifecycle.MongoCommonModelLifecycleListener;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleListener;
import io.extremum.mongo.springdata.EnableAllMongoAuditing;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.starter.DateToZonedDateTimeConverter;
import io.extremum.starter.DescriptorToStringConverter;
import io.extremum.starter.ZonedDateTimeToDateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableAllMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class MainMongoConfiguration extends AbstractMongoConfiguration {
    private final MongoProperties mongoProperties;

    @Bean
    public MongoClientURI mongoDatabaseUri() {
        return new MongoClientURI(mongoProperties.getServiceDbUri());
    }

    @Override
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        return super.mongoTemplate();
    }

    @Override
    @Bean
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
        // TODO: a better solution?
        // We explicitly return here an empty set to disable pre-scanning
        // for entities. If we allow the infrastructure to pre-scan, we would
        // have to exclude entities from different Mongo databases, and
        // it is not clear how to do it.
        // On the other hand, when Repository instances are created, indices
        // are created within a correct Mongo database (defined by MongoTemplate
        // which is in turn defined by Repository-scanning annotation like
        // @EnableMongoRepositories.
        return emptyMappingBasePackagesSetToAvoidMultipleDatasourceProblems();
    }

    private Collection<String> emptyMappingBasePackagesSetToAvoidMultipleDatasourceProblems() {
        return Collections.emptySet();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();

        converters.add(new DateToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateConverter());
        converters.add(new DescriptorToStringConverter());
        converters.add(new StorageTypeToStringConverter());
        converters.add(new StringToStorageTypeConverter());

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

    @WritingConverter
    private static class StorageTypeToStringConverter implements Converter<Descriptor.StorageType, String> {
        @Override
        public String convert(Descriptor.StorageType source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    private static class StringToStorageTypeConverter implements Converter<String, Descriptor.StorageType> {
        @Override
        public Descriptor.StorageType convert(String source) {
            return Descriptor.StorageType.fromString(source);
        }
    }
}
