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
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.urls.ApplicationUrlsImpl;
import com.extremum.everything.aop.ConvertNullDescriptorToModelNotFoundAspect;
import com.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import com.extremum.everything.aop.EverythingEverythingExceptionHandler;
import com.extremum.everything.config.listener.DefaultModelClasses;
import com.extremum.everything.config.listener.ModelClasses;
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
import com.extremum.everything.services.management.*;
import com.extremum.starter.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DestroyerProperties.class, ModelProperties.class})
@AutoConfigureAfter(CommonConfiguration.class)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
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
            EverythingCollectionManagementService collectionManagementService) {
        return new DefaultEverythingEverythingCollectionRestController(collectionManagementService);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingExceptionHandler.class)
    public EverythingEverythingExceptionHandler everythingEverythingExceptionHandler() {
        return new DefaultEverythingEverythingExceptionHandler();
    }

    @Bean
    public ConvertNullDescriptorToModelNotFoundAspect convertNullDescriptorToModelNotFoundAspect() {
        return new ConvertNullDescriptorToModelNotFoundAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalDao universalDao(MongoOperations mongoOperations) {
        return new SpringDataUniversalDao(mongoOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommonServices commonServices(List<CommonService<?, ? extends Model>> services) {
        return new DefaultCommonServices(services);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelClasses modelClasses() {
        return new DefaultModelClasses(modelProperties.getPackageNames());
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGetter<Model> defaultGetter(CommonServices commonServices, ModelClasses modelClasses) {
        return new DefaultGetterImpl<>(commonServices, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultPatcher<BasicModel<?>> defaultPatcher(
            DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator validator,
            List<CommonService<?, ? extends BasicModel<?>>> services,
            CommonServices commonServices,
            ModelClasses modelClasses,
            List<FromRequestDtoConverter<? extends BasicModel<?>, ? extends RequestDto>> dtoConverters
    ) {
        return new DefaultPatcherImpl<>(dtoConversionService, jsonMapper, emptyFieldDestroyer, validator,
                services, commonServices, modelClasses, dtoConverters);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultRemover defaultRemover(CommonServices commonServices, ModelClasses modelClasses) {
        return new DefaultRemoverImpl(commonServices, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingEverythingManagementService everythingEverythingManagementService(
            List<GetterService<? extends Model>> getterServices,
            List<PatcherService<? extends Model>> patcherServices,
            List<RemovalService> removalServices,
            DefaultGetter<Model> defaultGetter,
            DefaultPatcher<BasicModel<?>> defaultPatcher,
            DefaultRemover defaultRemover,
            List<CollectionFetcher> collectionFetchers,
            DtoConversionService dtoConversionService,
            UniversalDao universalDao) {
        return new DefaultEverythingEverythingManagementService(getterServices, patcherServices, removalServices,
                defaultGetter, defaultPatcher, defaultRemover,
                collectionFetchers, dtoConversionService, universalDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationUrls applicationUrls() {
        return new ApplicationUrlsImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionMakeup collectionMakeup(CollectionDescriptorService collectionDescriptorService,
            ApplicationUrls applicationUrls) {
        return new CollectionMakeupImpl(collectionDescriptorService, applicationUrls);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseCollectionsMakeupAdvice responseCollectionsMakeupAdvice(CollectionMakeup collectionMakeup) {
        return new ResponseCollectionsMakeupAdvice(collectionMakeup);
    }

    @Bean
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

    @Bean
    @ConditionalOnMissingBean(EverythingCollectionManagementService.class)
    public EverythingCollectionManagementService everythingCollectionManagementService(
            CollectionDescriptorService collectionDescriptorService,
            EverythingEverythingManagementService everythingEverythingManagementService
    ) {
        return new DefaultEverythingCollectionManagementService(collectionDescriptorService,
                everythingEverythingManagementService);
    }
}
