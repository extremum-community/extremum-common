package com.extremum.elastic.model;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.PersistableCommonModel;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class ElasticCommonModel implements PersistableCommonModel<String> {
    private Descriptor uuid;
    private String id;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private Long version;
    private Boolean deleted;
    private Long seqNo;
    private Long primaryTerm;
}
