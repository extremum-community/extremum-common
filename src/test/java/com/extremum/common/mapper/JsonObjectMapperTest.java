package com.extremum.common.mapper;

import com.extremum.common.descriptor.Descriptor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author rpuch
 */
public class JsonObjectMapperTest {
    private JsonObjectMapper mapper = JsonObjectMapper.createdMapper();

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
        Descriptor result = mapper.reader(Descriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }
}