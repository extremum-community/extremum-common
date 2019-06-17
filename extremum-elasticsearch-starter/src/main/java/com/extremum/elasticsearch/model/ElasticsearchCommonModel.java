package com.extremum.elasticsearch.model;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.elasticsearch.annotation.PrimaryTerm;
import com.extremum.elasticsearch.annotation.SequenceNumber;
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
    @SequenceNumber
    private Long seqNo;
    @PrimaryTerm
    private Long primaryTerm;
}
