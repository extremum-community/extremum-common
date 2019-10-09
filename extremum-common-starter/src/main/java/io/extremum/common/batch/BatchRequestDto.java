package io.extremum.common.batch;

import lombok.*;
import org.springframework.http.HttpMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class BatchRequestDto {
    private String endpoint;
    private String query;
    private HttpMethod method;
    private Object body;
}
