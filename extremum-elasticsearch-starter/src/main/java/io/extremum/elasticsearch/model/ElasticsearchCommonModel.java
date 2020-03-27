package io.extremum.elasticsearch.model;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.ZonedDateTime;

@Getter
@Setter
public abstract class ElasticsearchCommonModel implements PersistableCommonModel<String> {
    @Field(type = FieldType.Keyword)
    private Descriptor uuid;
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @CreatedDate
    @Field(type = FieldType.Date)
    private ZonedDateTime created;
    @LastModifiedDate
    @Field(type = FieldType.Date)
    private ZonedDateTime modified;
    @Version
    @Field(type = FieldType.Long)
    private Long version;
    @Field(type = FieldType.Boolean)
    private Boolean deleted;
    private Long seqNo;
    private Long primaryTerm;

    @Override
    public void copyServiceFieldsTo(Model to) {
        if (!(to instanceof ElasticsearchCommonModel)) {
            throw new IllegalStateException("I can only copy to a ElasticsearchCommonModel");
        }

        ElasticsearchCommonModel esTo = (ElasticsearchCommonModel) to;

        PersistableCommonModel.super.copyServiceFieldsTo(esTo);

        esTo.setSeqNo(this.getSeqNo());
        esTo.setPrimaryTerm(this.getPrimaryTerm());
    }
}
