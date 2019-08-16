package io.extremum.everything.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {EverythingControllersTestConfiguration.class,
        DefaultEverythingEverythingCollectionRestController.class})
@AutoConfigureMockMvc
class DefaultEverythingEverythingCollectionRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EverythingCollectionManagementService collectionManagementService;

    @Test
    void streamsCollection() throws Exception {
        when(collectionManagementService.streamCollection(anyString(), any(), anyBoolean()))
                .thenReturn(Flux.just(new TestResponseDto("first"), new TestResponseDto("second")));

        String responseText = mockMvc.perform(get("/stream-collection/dead-beef"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(not(isEmptyString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatFirstAndSecondAreReturned(responseText);
    }

    private void assertThatFirstAndSecondAreReturned(String responseText) {
        List<TestResponseDto> dtos = new NaiveTextEventStream(responseText).dataStream()
                .map(this::parseResponseDto)
                .collect(Collectors.toList());
        assertThat(dtos, hasSize(2));
        assertThat(dtos.get(0).name, is("first"));
        assertThat(dtos.get(1).name, is("second"));
    }

    private TestResponseDto parseResponseDto(String data) {
        try {
            return objectMapper.readerFor(TestResponseDto.class).readValue(data);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse", e);
        }
    }

    private static Descriptor forName(String name) {
        return Descriptor.builder()
                .externalId("external-id-" + name)
                .internalId("internal-id-" + name)
                .modelType("Test")
                .storageType(Descriptor.StorageType.MONGO)
                .build();
    }

    private static class TestResponseDto extends CommonResponseDto {
        @JsonProperty
        public String name;
        @JsonProperty
        public String model;

        @JsonCreator
        public TestResponseDto() {
        }

        TestResponseDto(String name) {
            this.name = name;

            setId(forName(name));
            setCreated(ZonedDateTime.now());
            setModified(ZonedDateTime.now());
            setVersion(1L);
        }

        @Override
        public String getModel() {
            return "Test";
        }
    }
}