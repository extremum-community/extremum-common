package io.extremum.everything.config;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.conversion.CollectionMakeup;
import io.extremum.common.collection.conversion.CollectionMakeupImpl;
import io.extremum.common.collection.conversion.ResponseCollectionsMakeupAdvice;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.spring.StringToCollectionDescriptorConverter;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.models.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.ScanningModelClasses;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.ApplicationUrlsImpl;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFoundAspect;
import io.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import io.extremum.everything.aop.EverythingEverythingExceptionHandler;
import io.extremum.everything.config.properties.DestroyerProperties;
import io.extremum.everything.config.properties.ModelProperties;
import io.extremum.everything.controllers.DefaultEverythingEverythingCollectionRestController;
import io.extremum.everything.controllers.DefaultEverythingEverythingRestController;
import io.extremum.everything.controllers.EverythingEverythingCollectionRestController;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.dao.SpringDataUniversalDao;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.destroyer.EmptyFieldDestroyerConfig;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.defaultservices.*;
import io.extremum.security.*;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.starter.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.authentication.api.interfaces.SecurityProvider;
import io.extremum.everything.services.*;
import io.extremum.everything.services.management.*;
import io.extremum.everything.support.CommonServices;
import io.extremum.everything.support.DefaultModelDescriptors;
import io.extremum.everything.support.ListBasedCommonServices;
import io.extremum.everything.support.ModelDescriptors;
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
    public ModelDescriptors modelDescriptors(ModelClasses modelClasses, DescriptorService descriptorService) {
        return new DefaultModelDescriptors(modelClasses, descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGetter defaultGetter(CommonServices commonServices, ModelDescriptors modelDescriptors) {
        return new DefaultGetterImpl(commonServices, modelDescriptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultSaver defaultSaver(CommonServices commonServices) {
        return new DefaultSaverImpl(commonServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultRemover defaultRemover(CommonServices commonServices, ModelDescriptors modelDescriptors) {
        return new DefaultRemoverImpl(commonServices, modelDescriptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelRetriever modelRetriever(List<GetterService<?>> getterServices,
                                         DefaultGetter defaultGetter) {
        return new ModelRetriever(getterServices, defaultGetter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelSaver modelSaver(List<SaverService<?>> saverServices, DefaultSaver defaultSaver) {
        return new ModelSaver(saverServices, defaultSaver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PatcherHooksCollection patcherHooksCollection(List<PatcherHooksService<?, ?>> patcherHooksServices) {
        return new PatcherHooksCollection(patcherHooksServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public Patcher patcher(
            DtoConversionService dtoConversionService,
            ObjectMapper objectMapper,
            EmptyFieldDestroyer emptyFieldDestroyer,
            RequestDtoValidator requestDtoValidator,
            PatcherHooksCollection hooksCollection
    ) {
        return new PatcherImpl(dtoConversionService, objectMapper,
                emptyFieldDestroyer, requestDtoValidator, hooksCollection);
    }

    @Bean
    @ConditionalOnMissingBean
    public PatchFlow patchFlow(
            ModelRetriever modelRetriever,
            Patcher patcher,
            ModelSaver modelSaver,
            DataSecurity dataSecurity,
            PatcherHooksCollection hooksCollection
    ) {
        return new PatchFlowImpl(modelRetriever, patcher, modelSaver,
                dataSecurity, hooksCollection);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleChecker roleChecker(SecurityProvider securityProvider) {
        return new SecurityProviderRoleChecker(securityProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public PrincipalSource principalSource(SecurityProvider securityProvider) {
        return new SecurityProviderPrincipalSource(securityProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleSecurity everythingRoleSecurity(RoleChecker roleChecker, ModelClasses modelClasses) {
        return new ModelAnnotationRoleSecurity(roleChecker, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSecurity everythingDataSecurity(List<DataAccessChecker<?>> checkers, RoleChecker roleChecker,
            PrincipalSource principalSource) {
        return new AccessCheckersDataSecurity(checkers, roleChecker, principalSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingEverythingManagementService everythingEverythingManagementService(
            ModelRetriever modelRetriever,
            List<RemovalService> removalServices,
            DefaultRemover defaultRemover,
            PatchFlow patchFlow,
            List<CollectionFetcher> collectionFetchers,
            DtoConversionService dtoConversionService,
            UniversalDao universalDao,
            RoleSecurity roleSecurity,
            DataSecurity dataSecurity) {
        EverythingEverythingManagementService service = new DefaultEverythingEverythingManagementService(
                modelRetriever,
                patchFlow, removalServices,
                defaultRemover,
                collectionFetchers, dtoConversionService, universalDao, dataSecurity);
        return new RoleSecurityEverythingEverythingManagementService(service, roleSecurity);
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
    @ConditionalOnMissingBean(EverythingCollectionManagementService.class)
    public EverythingCollectionManagementService everythingCollectionManagementService(
            CollectionDescriptorService collectionDescriptorService,
            EverythingEverythingManagementService everythingEverythingManagementService
    ) {
        return new DefaultEverythingCollectionManagementService(collectionDescriptorService,
                everythingEverythingManagementService);
    }
}
