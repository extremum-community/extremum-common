package com.extremum.elasticsearch.model;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.PersistableCommonModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class ElasticsearchCommonModel implements PersistableCommonModel<String> {
    @Transient
    private Descriptor uuid;
    @Id
    private String id;
    @CreatedDate
    private ZonedDateTime created;
    @LastModifiedDate
    private ZonedDateTime modified;
    @Version
    private Long version;
    private Boolean deleted;
    private Long seqNo;
    private Long primaryTerm;
}
