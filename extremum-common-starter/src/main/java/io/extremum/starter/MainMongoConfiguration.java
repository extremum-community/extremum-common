package io.extremum.starter;

import io.extremum.common.collection.spring.CollectionDescriptorLifecycleListener;
import io.extremum.common.repository.mongo.EnableAllMongoAuditing;
import io.extremum.starter.properties.MongoProperties;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
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
    private final MongoProperties mongoProps;
    private final MongoClientURI databaseUri;

    @Override
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        return super.mongoTemplate();
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        return new MongoClient(databaseUri);
    }

    @Override
    protected String getDatabaseName() {
        return mongoProps.getServiceDbName();
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

        return new MongoCustomConversions(converters);
    }

    @Bean
    public CollectionDescriptorLifecycleListener collectionDescriptorLifecycleListener() {
        return new CollectionDescriptorLifecycleListener();
    }
    
}
