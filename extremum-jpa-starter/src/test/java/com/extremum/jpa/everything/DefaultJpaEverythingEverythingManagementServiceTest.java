package com.extremum.jpa.everything;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.conversion.OwnedCollection;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.services.CollectionFetcher;
import com.extremum.everything.services.GetterService;
import com.extremum.everything.services.management.DefaultEverythingEverythingManagementService;
import com.extremum.jpa.models.PostgresBasicModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultJpaEverythingEverythingManagementServiceTest {
    @InjectMocks
    private DefaultEverythingEverythingManagementService service;

    @Spy
    private GetterService<JpaBasicContainer> jpaBasicContainerGetterService = new JpaBasicContainerGetter();
    @Mock
    private UniversalDao universalDao;
    @Mock
    private DtoConversionService dtoConversionService;
    private static ConfigurableApplicationContext context;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        context.start();
    }

    @AfterAll
    static void clear() {
        context.stop();
    }

    @BeforeEach
    void setUp() {
        service = new DefaultEverythingEverythingManagementService(
                Collections.singletonList(jpaBasicContainerGetterService),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(new ExplicitJpaBasicElementFetcher()),
                dtoConversionService,
                universalDao
        );
    }

    private void convertToResponseDtoWhenRequested() {
        when(dtoConversionService.convertUnknownToResponseDto(any(), any()))
                .thenReturn(mock(ResponseDto.class));
    }

    @Test
    void givenAJpaBasicContainerExists_whenACollectionIsFetched_thenItShouldBeReturned() {
        when(jpaBasicContainerGetterService.get("internalHostId")).thenReturn(new JpaBasicContainer());

        Descriptor hostId = jpaBasicContainerDescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "elements");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> dtos = service.fetchCollection(collectionDescriptor, projection, false);

        assertThat(dtos.elements(), hasSize(2));
    }

    private Descriptor jpaBasicContainerDescriptor() {
        return Descriptor.builder()
                .externalId("hostId")
                .internalId("internalHostId")
                .modelType("JpaBasicContainer")
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();
    }

    @Test
    void givenAnExplicitJpaBasicCollectionFetcherIsDefined_whenCollectionIsFetched_thenItShouldBeProvidedByTheFetcher() {
        convertToResponseDtoWhenRequested();

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(jpaBasicContainerDescriptor(),
                "explicitElements");
        Projection projection = Projection.empty();

        CollectionFragment<ResponseDto> elements = service.fetchCollection(collectionDescriptor,
                projection, false);

        assertThat(elements.elements(), hasSize(1));
    }

    @ModelName("JpaBasicElement")
    private static class JpaBasicElement extends PostgresBasicModel {
    }

    @ModelName("JpaBasicContainer")
    private static class JpaBasicContainer extends PostgresBasicModel {
        @OwnedCollection
        private List<JpaBasicElement> elements = Arrays.asList(new JpaBasicElement(), new JpaBasicElement());
        @OwnedCollection
        private List<JpaBasicElement> explicitElements;
    }

    private static class JpaBasicContainerGetter implements GetterService<JpaBasicContainer> {
        @Override
        public JpaBasicContainer get(String id) {
            return new JpaBasicContainer();
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }

    private static class ExplicitJpaBasicElementFetcher implements CollectionFetcher<JpaBasicContainer,
            JpaBasicElement> {

        @Override
        public String getHostPropertyName() {
            return "explicitElements";
        }

        @Override
        public CollectionFragment<JpaBasicElement> fetchCollection(JpaBasicContainer container, Projection projection) {
            return CollectionFragment.forCompleteCollection(Collections.singletonList(new JpaBasicElement()));
        }

        @Override
        public String getSupportedModel() {
            return "JpaBasicContainer";
        }
    }
}