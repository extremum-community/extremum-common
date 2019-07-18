package com.extremum.everything.services.management;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.descriptor.service.DBDescriptorLoader;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.models.Model;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import com.extremum.everything.MockedMapperDependencies;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.security.AllowEverythingForDataAccess;
import com.extremum.everything.services.*;
import com.extremum.everything.services.defaultservices.*;
import com.extremum.everything.support.*;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.descriptor.DescriptorLoader;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.collect.ImmutableList;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class EverythingServicesTest {
    private DefaultEverythingEverythingManagementService service;

    private static final UniversalDao NOT_USED = null;

    private final DtoConversionService dtoConversionService = new MockDtoConversionService();
    private final ObjectMapper objectMapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Mock
    private MongoCommonDao<MongoModelWithoutServices> commonDaoForModelWithoutServices;
    @Mock
    private DescriptorService descriptorService;
    @Spy
    private RemovalService mongoWithServicesRemovalService = new MongoWithServicesRemovalService();
    @Spy
    private PatcherService<MongoModelWithServices> mongoWithServicesPatcherService
            = new MongoWithServicesPatcherService(dtoConversionService, objectMapper);
    @Spy
    private SaverService<MongoModelWithServices> mongoWithServicesSaverService
            = new MongoWithServicesSaverService();
    @InjectMocks
    private DBDescriptorLoader descriptorLoader;

    private DescriptorLoader oldDescriptorLoader;

    private final Descriptor descriptor = new Descriptor("external-id");
    private final ObjectId objectId = new ObjectId();
    private final JsonPatch jsonPatch = new JsonPatch(emptyList());

    @BeforeEach
    void initDescriptorLoader() {
        oldDescriptorLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

    @BeforeEach
    void initEverythingServicesAndManagementService() {
        MongoCommonServiceImpl<MongoModelWithoutServices> commonServiceForMongoModelWithoutServices
                = new MongoCommonServiceImpl<MongoModelWithoutServices>(commonDaoForModelWithoutServices) {};
        CommonServices commonServices = new ListBasedCommonServices(
                ImmutableList.of(commonServiceForMongoModelWithoutServices));
        ModelClasses modelClasses = new ConstantModelClasses(ImmutableMap.of(
                MongoModelWithServices.class.getSimpleName(), MongoModelWithServices.class,
                MongoModelWithoutServices.class.getSimpleName(), MongoModelWithoutServices.class
        ));
        ModelDescriptors modelDescriptors = new DefaultModelDescriptors(modelClasses, descriptorService);

        List<GetterService<?>> getters = ImmutableList.of(new MongoWithServicesGetterService());
        List<SaverService<? extends Model>> savers = ImmutableList.of(mongoWithServicesSaverService);
        List<RemovalService> removers = ImmutableList.of(mongoWithServicesRemovalService);

        DefaultGetter defaultGetter = new DefaultGetterImpl(commonServices, modelDescriptors);
        DefaultSaver defaultSaver = new DefaultSaverImpl(commonServices);
        DefaultRemover defaultRemover = new DefaultRemoverImpl(commonServices, modelDescriptors);

        ModelRetriever modelRetriever = new ModelRetriever(getters, defaultGetter);
        ModelSaver modelSaver = new ModelSaver(savers, defaultSaver);
        Patcher patcher = new PatcherImpl(dtoConversionService,
                objectMapper, new PublicEmptyFieldDestroyer(), new DefaultRequestDtoValidator(),
                new PatcherHooksCollection(emptyList()));
        PatchFlow patchFlow = new PatchFlowImpl(modelRetriever, patcher, modelSaver,
                new AllowEverythingForDataAccess(), new PatcherHooksCollection(emptyList()));

        service = new DefaultEverythingEverythingManagementService(
                modelRetriever,
                patchFlow, removers,
                defaultRemover,
                emptyList(),
                dtoConversionService, NOT_USED,
                new AllowEverythingForDataAccess());
    }

    @AfterEach
    void restoreDescriptorLoader() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(oldDescriptorLoader);
    }

    @Test
    void givenAnEntityHasGetterService_whenGetting_thenGetterServiceShouldProvideTheResult() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        ResponseDto dto = service.get(descriptor, false);
        
        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithServices(dto);
    }

    private void assertThatDtoIsForModelWithServices(ResponseDto dto) {
        assertThat(dto, is(instanceOf(ResponseDtoForModelWithServices.class)));
    }

    private void whenGetDescriptorByExternalIdThenReturnOne(String modelName) {
        Descriptor descriptor = buildDescriptor(modelName);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(descriptor));
    }

    private Descriptor buildDescriptor(String modelName) {
        return Descriptor.builder()
                .externalId("external-id")
                .internalId(objectId.toString())
                .storageType(Descriptor.StorageType.MONGO)
                .modelType(modelName)
                .build();
    }

    @Test
    void givenAnEntityHasNoGetterService_whenGetting_thenCommonServiceShouldProvideTheResult() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();

        ResponseDto dto = service.get(descriptor, false);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithoutServices(dto);
    }

    private void whenFindByIdViaCommonServiceThenReturnAModel() {
        when(commonDaoForModelWithoutServices.findById(any())).thenReturn(Optional.of(new MongoModelWithoutServices()));
    }

    private void whenGetDescriptorByInternalIdThenReturnOne(String modelName) {
        Descriptor descriptor = buildDescriptor(modelName);
        when(descriptorService.loadByInternalId(objectId.toString())).thenReturn(Optional.of(descriptor));
    }
    
    private void assertThatDtoIsForModelWithoutServices(ResponseDto dto) {
        assertThat(dto, is(instanceOf(ResponseDtoForModelWithoutServices.class)));
    }

    @Test
    void givenAnEntityHasGetterService_whenPatching_thenShouldBeGotViaGetterService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        ResponseDto dto = service.patch(descriptor, jsonPatch, true);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithServices(dto);
    }

    @Test
    void givenAnEntityHasSaverService_whenPatching_thenShouldBeSavedViaSaverService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        service.patch(descriptor, jsonPatch, true);

        verify(mongoWithServicesSaverService).save(isA(MongoModelWithServices.class));
    }

    @Test
    void givenAnEntityHasNoPatcherService_whenPatching_thenShouldBePatchedViaCommonService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();
        when(commonDaoForModelWithoutServices.save(any())).then(interaction -> interaction.getArgument(0));

        ResponseDto dto = service.patch(descriptor, jsonPatch, true);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithoutServices(dto);
        verify(commonDaoForModelWithoutServices).save(any());
    }

    @Test
    void givenAnEntityHasRemovalService_whenDeleting_thenShouldBeRemovedViaRemovalService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        service.remove(descriptor);

        verify(mongoWithServicesRemovalService).remove(objectId.toString());
    }

    @Test
    void givenAnEntityHasNoRemovalService_whenDeleting_thenShouldBeRemovedViaCommonService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenFindByIdViaCommonServiceThenReturnAModel();

        service.remove(descriptor);

        verify(commonDaoForModelWithoutServices).deleteById(objectId);
    }

    private static class MongoWithServicesGetterService implements GetterService<MongoModelWithServices> {
        @Override
        public MongoModelWithServices get(String id) {
            return new MongoModelWithServices();
        }

        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }
    }

    private static class MongoWithServicesPatcherService extends AbstractPatcherService<MongoModelWithServices> {
        MongoWithServicesPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
            super(dtoConversionService, jsonMapper, new AllowEverythingForDataAccess());
        }

        @Override
        protected MongoModelWithServices persist(PatchPersistenceContext<MongoModelWithServices> context) {
            return context.getCurrentStateModel();
        }

        @Override
        protected MongoModelWithServices findById(String id) {
            return new MongoModelWithServices();
        }

        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }
    }

    private static class MongoWithServicesSaverService implements SaverService<MongoModelWithServices> {
        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }

        @Override
        public MongoModelWithServices save(MongoModelWithServices model) {
            return model;
        }
    }

    private static class MongoWithServicesRemovalService implements RemovalService {
        @Override
        public void remove(String id) {
        }

        @Override
        public String getSupportedModel() {
            return MongoModelWithServices.class.getSimpleName();
        }
    }
}
