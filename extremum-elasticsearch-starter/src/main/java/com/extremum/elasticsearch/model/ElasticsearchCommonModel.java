package com.extremum.elasticsearch.model;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.PersistableCommonModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class ElasticsearchCommonModel implements PersistableCommonModel<String> {
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
