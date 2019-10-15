package io.extremum.everything.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.collection.conversion.*;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.limit.ResponseLimiter;
import io.extremum.common.limit.ResponseLimiterAdvice;
import io.extremum.common.limit.ResponseLimiterImpl;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.support.ListBasedUniversalReactiveModelLoaders;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.common.tx.CollectionTransactor;
import io.extremum.common.tx.TransactorsCollectionTransactivity;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.urls.ApplicationUrlsImpl;
import io.extremum.everything.aop.ConvertNullDescriptorToModelNotFoundAspect;
import io.extremum.everything.aop.DefaultEverythingEverythingExceptionHandler;
import io.extremum.everything.aop.EverythingEverythingExceptionHandler;
import io.extremum.everything.config.properties.DestroyerProperties;
import io.extremum.everything.controllers.PingController;
import io.extremum.everything.dao.SpringDataUniversalDao;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.destroyer.EmptyFieldDestroyerConfig;
import io.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import io.extremum.everything.services.*;
import io.extremum.everything.services.collection.CollectionProviders;
import io.extremum.everything.services.collection.DefaultEverythingCollectionService;
import io.extremum.everything.services.collection.EverythingCollectionService;
import io.extremum.everything.services.collection.ListBasedCollectionProviders;
import io.extremum.everything.services.defaultservices.DefaultGetter;
import io.extremum.everything.services.defaultservices.DefaultReactiveGetter;
import io.extremum.everything.services.management.DefaultEverythingCollectionManagementService;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.PatcherHooksCollection;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.starter.properties.LimitsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DestroyerProperties.class, LimitsProperties.class})
public class EverythingCoreConfiguration {
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
    public UniversalReactiveModelLoaders universalReactiveModelLoader(List<UniversalReactiveModelLoader> loaders,
                                                                      ModelClasses modelClasses) {
        return new ListBasedUniversalReactiveModelLoaders(loaders, modelClasses);
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
    public PatcherHooksCollection patcherHooksCollection(List<PatcherHooksService<?, ?>> patcherHooksServices) {
        return new PatcherHooksCollection(patcherHooksServices);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionTransactivity collectionTransactivity(List<CollectionTransactor> transactors) {
        return new TransactorsCollectionTransactivity(transactors);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionProviders collectionProviders(
            List<OwnedCollectionFetcher> ownedCollectionFetchers,
            List<OwnedCollectionStreamer> ownedCollectionStreamers,
            List<FreeCollectionFetcher<? extends Model>> freeCollectionFetchers,
            List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers) {
        return new ListBasedCollectionProviders(ownedCollectionFetchers, ownedCollectionStreamers,
                freeCollectionFetchers, freeCollectionStreamers);
    }

    @Bean
    @ConditionalOnMissingBean
    public EverythingCollectionService everythingCollectionService(
            ModelRetriever modelRetriever,
            CollectionProviders collectionProviders,
            DtoConversionService dtoConversionService,
            UniversalDao universalDao, Reactifier reactifier,
            CollectionTransactivity transactivity) {
        return new DefaultEverythingCollectionService(modelRetriever, collectionProviders,
                dtoConversionService, universalDao, reactifier, transactivity);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationUrls applicationUrls() {
        return new ApplicationUrlsImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionUrls collectionUrls(ApplicationUrls applicationUrls) {
        return new CollectionUrlsInRoot(applicationUrls);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionMakeup collectionMakeup(CollectionDescriptorService collectionDescriptorService,
                                             ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
                                             CollectionUrls collectionUrls) {
        return new CollectionMakeupImpl(collectionDescriptorService,
                reactiveCollectionDescriptorService, collectionUrls);
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
    @ConditionalOnMissingBean
    public EverythingCollectionManagementService everythingCollectionManagementService(
            ReactiveCollectionDescriptorService reactiveCollectionDescriptorService,
            EverythingCollectionService everythingCollectionService
    ) {
        return new DefaultEverythingCollectionManagementService(
                reactiveCollectionDescriptorService, everythingCollectionService);
    }
}
