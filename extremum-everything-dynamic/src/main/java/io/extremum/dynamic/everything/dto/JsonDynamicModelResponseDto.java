package io.extremum.dynamic.everything.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.common.utils.DateUtils;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonDynamicModelResponseDto implements DynamicModelResponseDto<Map<String, Object>> {
    private Map<String, Object> data;
    private Descriptor id;

    @JsonIgnore
    public Long getVersion() {
        return extract(version.name(), Long.class).orElse(null);
    }

    @JsonIgnore
    public ZonedDateTime getCreated() {
        return extract(created.name(), String.class)
                .map(DateUtils::parseZonedDateTimeFromISO_8601)
                .orElseThrow(fieldNotPresented(created));
    }

    @JsonIgnore
    public ZonedDateTime getModified() {
        return extract(modified.name(), String.class)
                .map(DateUtils::parseZonedDateTimeFromISO_8601)
                .orElseThrow(fieldNotPresented(modified));
    }

    @JsonIgnore
    public String getModel() {
        return extract(model.name(), String.class)
                .orElseThrow(fieldNotPresented(model));
    }

    private Supplier<RuntimeException> fieldNotPresented(Model.FIELDS field) {
        return () -> {
            String msg = format("Field %s is not presented in model %s", field, this);
            log.error(msg);
            return new RuntimeException(msg);
        };
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
