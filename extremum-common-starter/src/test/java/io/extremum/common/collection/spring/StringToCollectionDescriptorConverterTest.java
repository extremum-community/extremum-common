package io.extremum.common.collection.spring;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class StringToCollectionDescriptorConverterTest {
    @InjectMocks
    private StringToCollectionDescriptorConverter converter;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;

    @Test
    void testConversion() {
        throw new UnsupportedOperationException("Not implemented yet");
//        when(collectionDescriptorService.retrieveByExternalId("external-id"))
//                .thenReturn(Optional.of(new CollectionDescriptor("external-id")));
//
//        CollectionDescriptor descriptor = converter.convert("external-id");
//        assertThat(descriptor, is(notNullValue()));
//        assertThat(descriptor.getExternalId(), is(equalTo("external-id")));
    }
}