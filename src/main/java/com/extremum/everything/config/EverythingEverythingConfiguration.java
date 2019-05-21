package com.extremum.everything.config;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.conversion.CollectionMakeup;
import com.extremum.common.collection.conversion.CollectionMakeupImpl;
import com.extremum.common.collection.conversion.ResponseCollectionsMakeupAdvice;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.collection.spring.StringToCollectionDescriptorConverter;
import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.FromRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.service.MongoCommonService;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.urls.ApplicationUrlsImpl;
import com.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import com.extremum.everything.aop.EverythingEverythingExceptionHandler;
import com.extremum.everything.config.listener.ModelClassesInitializer;
import com.extremum.everything.config.properties.DestroyerProperties;
import com.extremum.everything.config.properties.ModelProperties;
import com.extremum.everything.controllers.DefaultEverythingEverythingCollectionRestController;
import com.extremum.everything.controllers.DefaultEverythingEverythingRestController;
import com.extremum.everything.controllers.EverythingEverythingCollectionRestController;
import com.extremum.everything.controllers.EverythingEverythingRestController;
import com.extremum.everything.dao.SpringDataUniversalDao;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.EmptyFieldDestroyerConfig;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.services.*;
import com.extremum.everything.services.management.DefaultEverythingEverythingManagementService;
import com.extremum.everything.services.management.EverythingEverythingManagementService;
import com.extremum.everything.services.mongo.DefaultMongoGetterService;
import com.extremum.everything.services.mongo.DefaultMongoPatcherService;
import com.extremum.everything.services.mongo.DefaultMongoRemovalService;
import com.extremum.starter.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DestroyerProperties.class, ModelProperties.class})
@AutoConfigureAfter(CommonConfiguration.class)
@Import(DefaultServicesConfiguration.class)
public class EverythingEverythingConfiguration {
    private final ModelProperties modelProperties;
    private final DestroyerProperties destroyerProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("custom.field-destroyer.analyzable-package-prefix")
    public EmptyFieldDestroyer emptyFieldDestroyer() {
        EmptyFieldDestroyerConfig config = new EmptyFieldDestroyerConfig();
        config.setAnalyzablePackagePrefixes(destroyerProperties.getAnalyzablePackagePrefix());
        return new PublicEmptyFieldDestroyer(config);
    }

    @Bean
    @ConditionalOnMissingBean(RequestDtoValidator.class)
    public DefaultRequestDtoValidator requestDtoValidator() {
        return new DefaultRequestDtoValidator();
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingRestController.class)
    public DefaultEverythingEverythingRestController everythingEverythingRestController(
            EverythingEverythingManagementService service) {
        return new DefaultEverythingEverythingRestController(service);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingCollectionRestController.class)
    public DefaultEverythingEverythingCollectionRestController everythingEverythingCollectionRestController(
            EverythingEverythingManagementService service) {
        return new DefaultEverythingEverythingCollectionRestController(service);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingExceptionHandler.class)
    public EverythingEverythingExceptionHandler everythingEverythingExceptionHandler() {
        return new DefaultEverythingEverythingExceptionHandler();
    }

    @Bean
    @ConditionalOnBean(MongoOperations.class)
    @ConditionalOnMissingBean(UniversalDao.class)
    public UniversalDao universalDao(MongoOperations mongoOperations) {
        return new SpringDataUniversalDao(mongoOperations);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingManagementService.class)
    public EverythingEverythingManagementService everythingEverythingManagementService(List<GetterService<? extends Model>> getterServices,
                                                                                       List<PatcherService<? extends Model>> patcherServices,
                                                                                       List<RemovalService> removalServices,
                                                                                       List<CollectionFetcher> collectionFetchers,
                                                                                       DtoConversionService dtoConversionService,
                                                                                       UniversalDao universalDao) {
        return new DefaultEverythingEverythingManagementService(getterServices, patcherServices, removalServices,
                collectionFetchers, dtoConversionService, universalDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationUrls applicationUrls() {
        return new ApplicationUrlsImpl();
    }

    @Bean
    @ConditionalOnBean(CollectionDescriptorService.class)
    @ConditionalOnMissingBean
    public CollectionMakeup collectionMakeup(CollectionDescriptorService collectionDescriptorService) {
        return new CollectionMakeupImpl(collectionDescriptorService, applicationUrls());
    }

    @Bean
    @ConditionalOnBean(CollectionMakeup.class)
    @ConditionalOnMissingBean
    public ResponseCollectionsMakeupAdvice responseCollectionsMakeupAdvice(CollectionMakeup collectionMakeup) {
        return new ResponseCollectionsMakeupAdvice(collectionMakeup);
    }

    @Bean
    @ConditionalOnBean(CollectionDescriptorService.class)
    @ConditionalOnMissingBean
    public Converter<String, CollectionDescriptor> stringToCollectionDescriptorConverter(
            CollectionDescriptorService collectionDescriptorService) {
        return new StringToCollectionDescriptorConverter(collectionDescriptorService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "custom.model", value = "package-names")
    public ModelClassesInitializer modelNameToClassInitializer() {
        return new ModelClassesInitializer(modelProperties.getPackageNames());
    }
}
