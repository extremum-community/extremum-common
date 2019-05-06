package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ElasticData extends ElasticCommonModel {
    private String rawDocument;
    private String modelName;
    private Long seqNo;
    private Long primaryTerm;

    @Builder
    public ElasticData(Descriptor uuid, String id, ZonedDateTime created, ZonedDateTime modified, Long version,
                       Boolean deleted, String rawDocument, String modelName, Long seqNo, Long primaryTerm) {
        setUuid(uuid);
        setId(id);
        setCreated(created);
        setModified(modified);
        setVersion(version);
        setDeleted(deleted);

        this.rawDocument = rawDocument;
        this.modelName = modelName;
        this.seqNo = seqNo;
        this.primaryTerm = primaryTerm;
    }
}
