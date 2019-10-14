package io.extremum.everything.simple.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.authentication.api.SecurityProvider;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.everything.config.EverythingCoreConfiguration;
import io.extremum.everything.controllers.EverythingEverythingRestController;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.services.RemovalService;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.everything.services.SaverService;
import io.extremum.everything.services.defaultservices.*;
import io.extremum.everything.services.management.*;
import io.extremum.everything.simple.controller.DefaultEverythingEverythingRestController;
import io.extremum.everything.support.DefaultModelDescriptors;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.security.*;
import io.extremum.security.services.DataAccessChecker;
import io.extremum.starter.CommonConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@AutoConfigureAfter(CommonConfiguration.class)
@AutoConfigureBefore({WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class})
@Import(EverythingCoreConfiguration.class)
public class EverythingEverythingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EverythingGetDemultiplexer everythingDemultiplexer(
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
    // TODO: move to core?
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
    public ModelSaver modelSaver(List<SaverService<?>> saverServices, DefaultSaver defaultSaver) {
        return new ModelSaver(saverServices, defaultSaver);
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
    public RoleSecurity roleSecurity(RoleChecker roleChecker, ModelClasses modelClasses) {
        return new ModelAnnotationRoleSecurity(roleChecker, modelClasses);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSecurity dataSecurity(List<DataAccessChecker<?>> checkers, RoleChecker roleChecker,
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
}
