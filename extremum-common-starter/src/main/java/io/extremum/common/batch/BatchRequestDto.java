package io.extremum.common.batch;

import lombok.*;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
class BatchRequestDto {
    private String id;
    @NotNull
    @NotBlank
    private String endpoint;
    @NotNull
    private HttpMethod method;
    @Pattern(regexp = "\\?((?:([^\\s=]+)=([\\w-_]+)\\&)+)?(?:([^\\s=]+)=([\\w-_]+))",
            message = "Query params not valid!")
    private String query;
    private Object body;
}
