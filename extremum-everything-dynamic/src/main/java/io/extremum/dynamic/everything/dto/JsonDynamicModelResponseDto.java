package io.extremum.dynamic.everything.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonDynamicModelResponseDto implements DynamicModelResponseDto<JsonNode> {
    private JsonNode data;
    private Descriptor id;
    private Long version = 1L; // stub
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private String model;
}
