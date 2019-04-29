package com.extremum.common.collection.spring;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.service.CollectionDescriptorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class StringToCollectionDescriptorConverterTest {
    @InjectMocks
    private StringToCollectionDescriptorConverter converter;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;

    @Test
    public void testConversion() {
        when(collectionDescriptorService.retrieveByExternalId("external-id"))
                .thenReturn(Optional.of(new CollectionDescriptor("external-id")));

        CollectionDescriptor descriptor = converter.convert("external-id");
        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is(equalTo("external-id")));
    }
}