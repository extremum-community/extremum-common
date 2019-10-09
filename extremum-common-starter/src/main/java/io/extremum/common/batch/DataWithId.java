package io.extremum.common.batch;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataWithId {
    private String id;
    private Object data;
}
