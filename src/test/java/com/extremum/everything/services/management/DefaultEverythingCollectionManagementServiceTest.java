package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.response.Response;
import com.extremum.common.response.ResponseStatusEnum;
import com.extremum.everything.collection.Projection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DefaultEverythingCollectionManagementServiceTest {
    @InjectMocks
    private DefaultEverythingCollectionManagementService service;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;
    @Mock
    private EverythingEverythingManagementService everythingManagementService;
    @Mock
    private Collection<ResponseDto> expectedCollection;

    private final Projection emptyProjection = Projection.empty();

    @Test
    void givenACollectionDescriptorExists_whenFetchingTheCollection_thenTheCollectionShouldBeReturned() {
        when(collectionDescriptorService.retrieveByExternalId("collection-id"))
                .thenReturn(Optional.of(new CollectionDescriptor("collection-id")));
        when(everythingManagementService.fetchCollection(any(), any(), anyBoolean()))
                .thenReturn(expectedCollection);

        Response response = service.fetchCollection("collection-id", emptyProjection, true);

        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), is(ResponseStatusEnum.OK));
        assertThat(response.getCode(), is(200));
        assertThat(response.getResult(), is(instanceOf(Collection.class)));
        @SuppressWarnings("unchecked")
        Collection<ResponseDto> collection = (Collection<ResponseDto>) response.getResult();
        assertThat(collection, is(sameInstance(expectedCollection)));
    }

    @Test
    void givenACollectionDescriptorDoesNotExist_whenFetchingWithTheDescriptor_thenCollectionDescriptorNotFoundShouldBeThrown() {
        when(collectionDescriptorService.retrieveByExternalId("collection-id"))
                .thenReturn(Optional.empty());

        try {
            service.fetchCollection("collection-id", emptyProjection, true);
            fail("An exception should be thrown");
        } catch (CollectionDescriptorNotFoundException e) {
            assertThat(e.getMessage(), is("Did not find a collection descriptor by externalId 'collection-id'"));
        }
    }
}