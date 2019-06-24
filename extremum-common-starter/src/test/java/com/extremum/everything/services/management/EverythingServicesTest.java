package com.extremum.everything.services.management;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.Descriptor.StorageType;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.mapper.MockedMapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.extremum.common.models.Model;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.collect.ImmutableList;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private DescriptorDao descriptorDao;
    @Spy
    private RemovalService mongoWithServicesRemovalService = new MongoWithServicesRemovalService();
    @Spy
    private PatcherService<MongoModelWithServices> mongoWithServicesPatcherService
            = new MongoWithServicesPatcherService(dtoConversionService, objectMapper);

    private DescriptorDao oldDescriptorDao;

    private final Descriptor descriptor = new Descriptor("external-id");
    private final ObjectId objectId = new ObjectId();
    private final JsonPatch jsonPatch = new JsonPatch(Collections.emptyList());

    @BeforeEach
    void initDescriptorDao() {
        oldDescriptorDao = DescriptorService.getDescriptorDao();
        DescriptorService.setDescriptorDao(descriptorDao);
    }

    @BeforeEach
    void initEverythingServicesAndManagementService() {
        MongoCommonServiceImpl<MongoModelWithoutServices> commonServiceForMongoModelWithoutServices
                = new MongoCommonServiceImpl<MongoModelWithoutServices>(commonDaoForModelWithoutServices) {};
        CommonServices commonServices = new DefaultCommonServices(
                ImmutableList.of(commonServiceForMongoModelWithoutServices));
        ModelClasses modelClasses = new ConstantModelClasses(ImmutableMap.of(
                MongoModelWithServices.class.getSimpleName(), MongoModelWithServices.class,
                MongoModelWithoutServices.class.getSimpleName(), MongoModelWithoutServices.class
        ));

        List<GetterService<? extends Model>> getters = ImmutableList.of(new MongoWithServicesGetterService());
        List<PatcherService<? extends Model>> patchers = ImmutableList.of(mongoWithServicesPatcherService);
        List<RemovalService> removers = ImmutableList.of(mongoWithServicesRemovalService);

        DefaultGetter<Model> defaultGetter = new DefaultGetterImpl<>(commonServices, modelClasses);
        DefaultPatcher<Model> defaultPatcher = new DefaultPatcherImpl<>(
                dtoConversionService, objectMapper, new PublicEmptyFieldDestroyer(), new DefaultRequestDtoValidator(),
                commonServices, modelClasses, defaultGetter,
                ImmutableList.of(new DtoConverterForModelWithoutServices())
        );
        DefaultRemover defaultRemover = new DefaultRemoverImpl(commonServices, modelClasses);

        service = new DefaultEverythingEverythingManagementService(getters, patchers, removers,
                defaultGetter, defaultPatcher, defaultRemover,
                Collections.emptyList(),
                dtoConversionService, NOT_USED);
    }

    @AfterEach
    void restoreDescriptorDao() {
        DescriptorService.setDescriptorDao(oldDescriptorDao);
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
        when(descriptorDao.retrieveByExternalId("external-id")).thenReturn(Optional.of(descriptor));
    }

    private Descriptor buildDescriptor(String modelName) {
        return Descriptor.builder()
                .externalId("external-id")
                .internalId(objectId.toString())
                .storageType(StorageType.MONGO)
                .modelType(modelName)
                .build();
    }

    @Test
    void givenAnEntityHasNoGetterService_whenGetting_thenCommonServiceShouldProvideTheResult() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        when(commonDaoForModelWithoutServices.findById(any())).thenReturn(Optional.of(new MongoModelWithoutServices()));

        ResponseDto dto = service.get(descriptor, false);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithoutServices(dto);
    }

    private void whenGetDescriptorByInternalIdThenReturnOne(String modelName) {
        Descriptor descriptor = buildDescriptor(modelName);
        when(descriptorDao.retrieveByInternalId(objectId.toString())).thenReturn(Optional.of(descriptor));
    }
    
    private void assertThatDtoIsForModelWithoutServices(ResponseDto dto) {
        assertThat(dto, is(instanceOf(ResponseDtoForModelWithoutServices.class)));
    }

    @Test
    void givenAnEntityHasPatcherService_whenPatching_thenShouldBePatchedViaPatcherService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithServices.class.getSimpleName());

        ResponseDto dto = service.patch(descriptor, jsonPatch, true);

        assertThat(dto, is(notNullValue()));
        assertThatDtoIsForModelWithServices(dto);
        verify(mongoWithServicesPatcherService).patch(objectId.toString(), jsonPatch);
    }

    @Test
    void givenAnEntityHasNoPatcherService_whenPatching_thenShouldBePatchedViaCommonService() {
        whenGetDescriptorByExternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        whenGetDescriptorByInternalIdThenReturnOne(MongoModelWithoutServices.class.getSimpleName());
        when(commonDaoForModelWithoutServices.findById(any())).thenReturn(Optional.of(new MongoModelWithoutServices()));
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
        MongoWithServicesPatcherService(
                DtoConversionService dtoConversionService,
                ObjectMapper jsonMapper) {
            super(dtoConversionService, jsonMapper);
        }

        @Override
        protected MongoModelWithServices persist(PatchPersistenceContext<MongoModelWithServices> context,
                String modelName) {
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
