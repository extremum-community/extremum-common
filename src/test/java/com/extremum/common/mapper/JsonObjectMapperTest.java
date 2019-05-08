package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.StringWriter;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
public class JsonObjectMapperTest {
    private MockedMapperDependencies mapperDependencies = new MockedMapperDependencies();
    private JsonObjectMapper mapper = JsonObjectMapper.createWithCollectionDescriptors(mapperDependencies);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenDescriptorIsSerialized_thenTheResultShouldBeAStringLiteralOfExternalId() throws Exception {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .internalId("internal-id")
                .storageType(Descriptor.StorageType.MONGO)
                .modelType("test-model")
                .build();

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, descriptor);
        
        assertThat(writer.toString(), is("\"external-id\""));
    }

    @Test
    public void whenDescriptorIsDeserializedFromAString_thenDescriptorObjectShouldBeTheResult() throws Exception {
        Descriptor result = mapper.readerFor(Descriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }

    @Test
    public void whenCollectionDescriptorIsSerialized_thenTheResultShouldBeAStringLiteralOfExternalId() throws Exception {
        CollectionDescriptor descriptor = new CollectionDescriptor("external-id");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, descriptor);

        assertThat(writer.toString(), is("\"external-id\""));
    }

    @Test
    public void given_collectionDescriptorExists_whenCollectionDescriptorIsDeserializedFromAString_thenCollectionDescriptorObjectShouldBeTheResult()
            throws Exception {
        when(mapperDependencies.collectionDescriptorService().retrieveByExternalId("external-id"))
                .thenReturn(Optional.of(new CollectionDescriptor("external-id")));

        CollectionDescriptor result = mapper.readerFor(CollectionDescriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }

    @Test
    public void whenCollectionDescriptorIsDeserializedFromAString_thenCollectionDescriptorObjectShouldBeTheResult()
            throws Exception {
        when(mapperDependencies.collectionDescriptorService().retrieveByExternalId("external-id"))
                .thenReturn(Optional.empty());

        expectedException.expect(CollectionDescriptorNotFoundException.class);
        expectedException.expectMessage("No collection descriptor was found by external ID 'external-id'");

        mapper.readerFor(CollectionDescriptor.class).readValue("\"external-id\"");
    }
}