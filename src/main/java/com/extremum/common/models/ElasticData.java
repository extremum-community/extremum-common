package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ElasticData extends ElasticCommonModel {
    private String rawDocument;
    private Descriptor uuid;
    private String id;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private Long version;
    private Boolean deleted;
    private String modelName;
    private Long seqNo;
    private Long primaryTerm;

    @Override
    public String getModelName() {
        return modelName;
    }

    public synchronized long incrementAndGetVersion() {
        version++;
        return version;
    }
}
