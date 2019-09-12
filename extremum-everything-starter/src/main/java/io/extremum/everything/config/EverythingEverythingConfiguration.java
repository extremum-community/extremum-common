package io.extremum.everything.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.authentication.api.SecurityProvider;
import io.extremum.common.collection.conversion.CollectionMakeup;
import io.extremum.common.collection.conversion.CollectionMakeupImpl;
import io.extremum.common.collection.conversion.ReactiveResponseCollectionsMakeupAspect;
import io.extremum.common.collection.conversion.ResponseCollectionsMakeupAdvice;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.limit.ResponseLimiter;
import io.extremum.common.limit.ResponseLimiterAdvice;
import io.extremum.common.limit.ResponseLimiterImpl;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.support.*;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.common.tx.CollectionTransactor;
import io.extremum.common.tx.TransactorsCollectionTransactivity;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.ApplicationUrlsImpl;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFoundAspect;
import io.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import io.extremum.everything.aop.EverythingEverythingExceptionHandler;
import io.extremum.everything.config.properties.DestroyerProperties;
import io.extremum.everything.controllers.DefaultEverythingEverythingRestController;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.controllers.PingController;
import io.extremum.everything.dao.SpringDataUniversalDao;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.destroyer.EmptyFieldDestroyerConfig;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.*;
import io.extremum.everything.services.defaultservices.*;
import io.extremum.everything.services.management.*;
import io.extremum.everything.support.DefaultModelDescriptors;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.security.*;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.starter.CommonConfiguration;
import io.extremum.starter.properties.LimitsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DestroyerProperties.class, LimitsProperties.class})
@AutoConfigureAfter(CommonConfiguration.class)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class EverythingEverythingConfiguration {
    private final DestroyerProperties destroyerProperties;
    private final LimitsProperties limitsProperties;

    @Bean
    @ConditionalOnMissingBean
    public EmptyFieldDestroyer emptyFieldDestroyer() {
        EmptyFieldDestroyerConfig config = new EmptyFieldDestroyerConfig();
        config.setAnalyzablePackagePrefixes(destroyerProperties.getAnalyzablePackagePrefix());
        return new PublicEmptyFieldDestroyer(config);
    }

    @Bean
    @ConditionalOnMissingBean(RequestDtoValidator.class)
    public RequestDtoValidator requestDtoValidator() {
        return new DefaultRequestDtoValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingGetDemultiplexer everythingMultiplexer(
            EverythingEverythingManagementService everythingManagementService,
            EverythingCollectionManagementService everythingCollectionManagementService) {
        return new EverythingGetDemultiplexerOnDescriptor(everythingManagementService,
                everythingCollectionManagementService);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingEverythingRestController.class)
    public DefaultEverythingEverythingRestController everythingEverythingRestController(
            EverythingEverythingManagementService everythingManagementService,
            EverythingCollectionManagementService everythingCollectionManagementService,
            EverythingGetDemultiplexer multiplexer) {
        return new DefaultEverythingEverythingRestController(everythingManagementService,
                everythingCollectionManagementService, multiplexer);
    }

    @Bean
    @ConditionalOnMissingBean
    public PingController pingController() {
        return new PingController();
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
    public UniversalDao universalDao(MongoOperations mongoOperations,
                                     ReactiveMongoOperations reactiveMongoOperations) {
        return new SpringDataUniversalDao(mongoOperations, reactiveMongoOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelDescriptors modelDescriptors(ModelClasses modelClasses, DescriptorService descriptorService) {
        return new DefaultModelDescriptors(modelClasses, descriptorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalReactiveModelLoaders universalReactiveModelLoader(List<UniversalReactiveModelLoader> loaders,
                                                                      ModelClasses modelClasses) {
        return new ListBasedUniversalReactiveModelLoaders(loaders, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGetter defaultGetter(CommonServices commonServices, ModelDescriptors modelDescriptors) {
        return new DefaultGetterImpl(commonServices, modelDescriptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultReactiveGetter defaultReactiveGetter(
            ReactiveDescriptorService reactiveDescriptorService,
            UniversalReactiveModelLoaders universalReactiveModelLoader) {
        return new DefaultReactiveGetterImpl(reactiveDescriptorService, universalReactiveModelLoader);
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
                                         List<ReactiveGetterService<?>> reactiveGetterServices,
                                         DefaultGetter defaultGetter,
                                         DefaultReactiveGetter defaultReactiveGetter) {
        return new ModelRetriever(getterServices, reactiveGetterServices, defaultGetter, defaultReactiveGetter);
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
            PatchFlow patchFlow,
            List<RemovalService> removalServices,
            DefaultRemover defaultRemover,
            DtoConversionService dtoConversionService,
            RoleSecurity roleSecurity,
            DataSecurity dataSecurity) {
        EverythingEverythingManagementService service = new DefaultEverythingEverythingManagementService(
                modelRetriever,
                patchFlow, removalServices,
                defaultRemover,
                dtoConversionService, dataSecurity);
        return new RoleSecurityEverythingEverythingManagementService(service, roleSecurity);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionTransactivity collectionTransactivity(List<CollectionTransactor> transactors) {
        return new TransactorsCollectionTransactivity(transactors);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingCollectionService everythingCollectionService(
            ModelRetriever modelRetriever,
            List<CollectionFetcher> collectionFetchers,
            List<CollectionStreamer> collectionStreamers,
            DtoConversionService dtoConversionService,
            UniversalDao universalDao, Reactifier reactifier,
            CollectionTransactivity transactivity) {
        return new DefaultEverythingCollectionService(modelRetriever, collectionFetchers,
                collectionStreamers, dtoConversionService, universalDao, reactifier, transactivity);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationUrls applicationUrls() {
        return new ApplicationUrlsImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionMakeup collectionMakeup(DescriptorSaver descriptorSaver,
                                             CollectionDescriptorService collectionDescriptorService,
                                             ReactiveDescriptorSaver reactiveDescriptorSaver,
                                             ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
                                             ApplicationUrls applicationUrls) {
        return new CollectionMakeupImpl(descriptorSaver, collectionDescriptorService,
                reactiveDescriptorSaver, reactiveCollectionDescriptorService, applicationUrls);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseCollectionsMakeupAdvice responseCollectionsMakeupAdvice(CollectionMakeup collectionMakeup) {
        return new ResponseCollectionsMakeupAdvice(collectionMakeup);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveResponseCollectionsMakeupAspect reactiveResponseCollectionsMakeupAspect(
            CollectionMakeup collectionMakeup) {
        return new ReactiveResponseCollectionsMakeupAspect(collectionMakeup);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseLimiter responseLimiter(ObjectMapper objectMapper) {
        return new ResponseLimiterImpl(limitsProperties.getCollectionTopMaxSizeBytes(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseLimiterAdvice responseLimiterAdvice(ResponseLimiter limiter) {
        return new ResponseLimiterAdvice(limiter);
    }

    @Bean
    @ConditionalOnMissingBean(EverythingCollectionManagementService.class)
    public EverythingCollectionManagementService everythingCollectionManagementService(
            ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
            EverythingCollectionService everythingCollectionService
    ) {
        return new DefaultEverythingCollectionManagementService(
                reactiveCollectionDescriptorService, everythingCollectionService);
    }
}
