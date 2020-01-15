package io.extremum.dynamic.everything.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.common.utils.DateUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonDynamicModelResponseDto implements DynamicModelResponseDto<Map<String, Object>> {
    private Map<String, Object> data;
    private Descriptor id;

    @JsonIgnore
    public Long getVersion() {
        return extract("version", Long.class).orElse(null);
    }

    @JsonIgnore
    public ZonedDateTime getCreated() {
        return extract("created", String.class)
                .map(DateUtils::parseZonedDateTimeFromISO_8601).orElse(null);
    }

    @JsonIgnore
    public ZonedDateTime getModified() {
        return extract("modified", String.class)
                .map(DateUtils::parseZonedDateTimeFromISO_8601).orElse(null);
    }

    @JsonIgnore
    public String getModel() {
        return extract("model", String.class).orElse(null);
    }

    private <T> Optional<T> extract(String field, Class<T> classType) {
        return ofNullable(data.get(field))
                .filter(classType::isInstance)
                .map(classType::cast);
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }
}
