package io.extremum.mongo.config;

import com.mongodb.WriteConcern;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.springdata.DescriptorsMongoDb;
import io.extremum.mongo.springdata.MainMongoDb;
import io.extremum.mongo.springdata.MongoTemplateWithFixedDatabase;
import io.extremum.mongo.springdata.repository.SoftDeleteMongoRepositoryFactoryBean;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableMongoRepositories(basePackageClasses = {DescriptorRepository.class},
        repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class,
        mongoTemplateRef = "descriptorsMongoTemplate"
)
public class DescriptorsMongoConfiguration {
    private final MongoCustomConversions customConversions;
    private final MongoProperties mongoProperties;

    @Bean
    @DescriptorsMongoDb
    public MongoTemplate descriptorsMongoTemplate(@MainMongoDb MongoDbFactory mainMongoDbFactory,
            @DescriptorsMongoDb MappingMongoConverter descriptorsMappingMongoConverter) {
        MongoTemplate template = new MongoTemplateWithFixedDatabase(mainMongoDbFactory,
                descriptorsMappingMongoConverter, getDatabaseName());
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    private String getDatabaseName() {
        return mongoProperties.getDescriptorsDbName();
    }

    @Bean
    @DescriptorsMongoDb
    public MappingMongoConverter descriptorsMappingMongoConverter() throws Exception {

        // We do not allow DBRefs because they are pain (at least I was told so)
        DbRefResolver dbRefResolver = NoOpDbRefResolver.INSTANCE;
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, descriptorsMongoMappingContext());
        converter.setCustomConversions(customConversions);

        return converter;
    }

    @Bean
    public MongoMappingContext descriptorsMongoMappingContext() throws ClassNotFoundException {
        CustomDescriptorCollectionMappingContext mappingContext = new CustomDescriptorCollectionMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(fieldNamingStrategy());
        return mappingContext;
    }

    private Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {

        Set<Class<?>> initialEntitySet = new HashSet<>();

        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

    private Collection<String> getMappingBasePackages() {
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

    private Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {

        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        Set<Class<?>> initialEntitySet = new HashSet<>();

        if (StringUtils.hasText(basePackage)) {

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {

                initialEntitySet
                        .add(ClassUtils.forName(candidate.getBeanClassName(),
                                MongoConfigurationSupport.class.getClassLoader()));
            }
        }

        return initialEntitySet;
    }

    private boolean abbreviateFieldNames() {
        return false;
    }

    private FieldNamingStrategy fieldNamingStrategy() {
        return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
                : PropertyNameFieldNamingStrategy.INSTANCE;
    }

}
