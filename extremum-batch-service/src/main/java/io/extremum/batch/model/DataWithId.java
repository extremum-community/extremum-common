package io.extremum.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataWithId {
    private String id;
    private Object data;
}
