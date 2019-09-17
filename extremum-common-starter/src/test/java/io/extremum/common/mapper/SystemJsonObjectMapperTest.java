package io.extremum.common.mapper;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.AlertLevelEnum;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
        MatcherAssert.assertThat(descriptor.getDisplay().getType(), CoreMatchers.is(Display.Type.OBJECT));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon(), is(notNullValue()));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getType(), CoreMatchers.is(MediaType.IMAGE));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getUrl(), is("/url/to/resource"));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getDepth(), is(2));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getDuration(), is(notNullValue()));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getDuration().isInteger(), is(true));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getDuration().getIntegerValue(), is(20));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getWidth(), is(100));
        MatcherAssert.assertThat(descriptor.getDisplay().getIcon().getHeight(), is(200));
    }

    @Test
    void givenASerializedAlertWithAnError_whenDeserializingIt_thenItShouldBeDeserializedSuccessfully()
            throws Exception {
        Alert alert = Alert.errorAlert("Oops");
        String json = mapper.writerFor(Alert.class).writeValueAsString(alert);

        Alert deserializedAlert = mapper.readerFor(Alert.class).readValue(json);

        assertThat(deserializedAlert.isError(), is(true));
        assertThat(deserializedAlert.getMessage(), is("Oops"));
        MatcherAssert.assertThat(deserializedAlert.getLevel(), CoreMatchers.is(AlertLevelEnum.ERROR));
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