package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import com.extremum.common.response.Alert;
import com.extremum.common.response.AlertLevelEnum;
import com.extremum.common.response.Pagination;
import com.extremum.common.stucts.Display;
import com.extremum.common.stucts.MediaType;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
class SystemJsonObjectMapperTest {
    private MockedMapperDependencies mapperDependencies = new MockedMapperDependencies();
    private SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(mapperDependencies);

    @Test
    void whenDescriptorIsSerialized_thenTheResultShouldBeAStringLiteralOfExternalId() throws Exception {
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
    void whenDescriptorIsDeserializedFromAString_thenDescriptorObjectShouldBeTheResult() throws Exception {
        Descriptor result = mapper.readerFor(Descriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }

    @Test
    void whenCollectionDescriptorIsSerialized_thenTheResultShouldBeAStringLiteralOfExternalId() throws Exception {
        CollectionDescriptor descriptor = new CollectionDescriptor("external-id");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, descriptor);

        assertThat(writer.toString(), is("\"external-id\""));
    }

    @Test
    void givenCollectionDescriptorExists_whenCollectionDescriptorIsDeserializedFromAString_thenCollectionDescriptorObjectShouldBeTheResult()
            throws Exception {
        when(mapperDependencies.collectionDescriptorService().retrieveByExternalId("external-id"))
                .thenReturn(Optional.of(new CollectionDescriptor("external-id")));

        CollectionDescriptor result = mapper.readerFor(CollectionDescriptor.class).readValue("\"external-id\"");
        assertThat(result.getExternalId(), is("external-id"));
    }

    @Test
    void whenCollectionDescriptorIsDeserializedFromAString_thenCollectionDescriptorObjectShouldBeTheResult()
            throws Exception {
        when(mapperDependencies.collectionDescriptorService().retrieveByExternalId("external-id"))
                .thenReturn(Optional.empty());

        try {
            mapper.readerFor(CollectionDescriptor.class).readValue("\"external-id\"");
            fail("An exception should be thrown");
        } catch (CollectionDescriptorNotFoundException e) {
            assertThat(e.getMessage(), is("No collection descriptor was found by external ID 'external-id'"));
        }
    }

    @Test
    void givenMapperIsConfiguredWithoutDescriptorsTransfiguration_whenDeserializaingDescriptorWithDisplayAndIcon_thenItShouldBeOk()
            throws Exception {
        String json = "{\"created\":\"2019-05-24T15:54:59.958+0400\",\"deleted\":false,\"display\":" +
                "{\"caption\":\"aaa\",\"icon\":{\"depth\":2,\"duration\":20,\"height\":200,\"type\":\"image\",\"url\"" +
                ":\"/url/to/resource\",\"width\":100},\"splash\":{\"depth\":2,\"duration\":20,\"height\":200,\"type\":" +
                "\"image\",\"url\":\"/url/to/resource\",\"width\":100}},\"externalId\":" +
                "\"1e71af1b-16f8-4567-9660-9e4549a0203f\",\"internalId\":\"5ce7db93dde97936c6c4c302\",\"modelType\":" +
                "\"test_model\",\"modified\":\"2019-05-24T15:54:59.958+0400\",\"storageType\":\"mongo\",\"version\":0}";

        Descriptor descriptor = new BasicJsonObjectMapper()
                .readerFor(Descriptor.class).readValue(json);

        assertThat(descriptor.getDisplay(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getType(), is(Display.Type.OBJECT));
        assertThat(descriptor.getDisplay().getIcon(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getIcon().getType(), is(MediaType.IMAGE));
        assertThat(descriptor.getDisplay().getIcon().getUrl(), is("/url/to/resource"));
        assertThat(descriptor.getDisplay().getIcon().getDepth(), is(2));
        assertThat(descriptor.getDisplay().getIcon().getDuration(), is(notNullValue()));
        assertThat(descriptor.getDisplay().getIcon().getDuration().isInteger(), is(true));
        assertThat(descriptor.getDisplay().getIcon().getDuration().getIntegerValue(), is(20));
        assertThat(descriptor.getDisplay().getIcon().getWidth(), is(100));
        assertThat(descriptor.getDisplay().getIcon().getHeight(), is(200));
    }

    @Test
    void givenASerializedAlertWithAnError_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        Alert alert = Alert.errorAlert("Oops");
        String json = mapper.writerFor(Alert.class).writeValueAsString(alert);

        Alert deserializedAlert = mapper.readerFor(Alert.class).readValue(json);

        assertThat(deserializedAlert.isError(), is(true));
        assertThat(deserializedAlert.getMessage(), is("Oops"));
        assertThat(deserializedAlert.getLevel(), is(AlertLevelEnum.ERROR));
    }

    @Test
    void givenASerializedPagination_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        ZonedDateTime since = ZonedDateTime.now();
        ZonedDateTime until = since.plusYears(1);
        Pagination pagination = Pagination.builder()
                .count(10)
                .offset(20)
                .total(100L)
                .since(since)
                .until(until)
                .build();
        String json = mapper.writerFor(Pagination.class).writeValueAsString(pagination);

        Pagination deserializedPagination = mapper.readerFor(Pagination.class).readValue(json);

        assertThat(deserializedPagination.getCount(), is(10));
        assertThat(deserializedPagination.getOffset(), is(20));
        assertThat(deserializedPagination.getTotal(), is(100L));
        assertThat(deserializedPagination.getSince().toInstant(), is(since.toInstant()));
        assertThat(deserializedPagination.getUntil().toInstant(), is(until.toInstant()));
    }

    @Test
    void givenASerializedPaginationWithoutTotal_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        ZonedDateTime since = ZonedDateTime.now();
        ZonedDateTime until = since.plusYears(1);
        Pagination pagination = Pagination.builder()
                .count(10)
                .offset(20)
                .since(since)
                .until(until)
                .build();
        String json = mapper.writerFor(Pagination.class).writeValueAsString(pagination);

        Pagination deserializedPagination = mapper.readerFor(Pagination.class).readValue(json);

        assertThat(deserializedPagination.getTotal(), is(nullValue()));
    }
}