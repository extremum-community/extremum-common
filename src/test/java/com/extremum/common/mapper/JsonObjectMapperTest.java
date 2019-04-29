package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import org.junit.Test;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rpuch
 */
public class JsonObjectMapperTest {
    private JsonObjectMapper mapper = JsonObjectMapper.createMapper(new MockedMapperDependencies());

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
    public void whenCollectionDescriptorIsDeserializedFromAString_thenCollectionDescriptorObjectShouldBeTheResult()
            throws Exception {
        CollectionDescriptor result = mapper.readerFor(CollectionDescriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }
}