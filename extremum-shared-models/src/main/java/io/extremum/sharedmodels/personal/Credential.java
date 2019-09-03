package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "value")
@DocumentationName("Credential")
public class Credential {
    @JsonProperty("system")
    private String system;

    @JsonProperty("name")
    private VerifyType type;

    @JsonProperty("value")
    private String value;
}
