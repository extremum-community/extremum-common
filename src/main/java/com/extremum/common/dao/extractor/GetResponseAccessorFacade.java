package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.DateUtils;
import org.elasticsearch.action.get.GetResponse;

import java.time.ZonedDateTime;

import static java.util.Optional.ofNullable;

public class GetResponseAccessorFacade implements AccessorFacade {
    private GetResponse response;

    public GetResponseAccessorFacade(GetResponse response) {
        this.response = response;
    }

    @Override
    public String getId() {
        return response.getId();
    }

    @Override
    public Descriptor getUuid() {
        return DescriptorService.loadByInternalId(response.getId()).get();
    }

    @Override
    public Long getVersion() {
        return response.getVersion();
    }

    @Override
    public Boolean getDeleted() {
        return ofNullable(response.getSourceAsMap())
                .map(m -> m.get(PersistableCommonModel.FIELDS.deleted.name()))
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    @Override
    public ZonedDateTime getCreated() {
        return ofNullable(response.getSourceAsMap())
                .map(m -> m.get(PersistableCommonModel.FIELDS.created.name()))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(v -> DateUtils.parseZonedDateTime(v, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .orElse(null);
    }

    @Override
    public ZonedDateTime getModified() {
        return ofNullable(response.getSourceAsMap())
                .map(m -> m.get(PersistableCommonModel.FIELDS.created.name()))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(v -> DateUtils.parseZonedDateTime(v, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .orElse(null);
    }

    @Override
    public String getRawSource() {
        return response.getSourceAsString();
    }

    @Override
    public Long getSeqNo() {
        return response.getSeqNo();
    }

    @Override
    public Long getPrimaryTerm() {
        return response.getPrimaryTerm();
    }
}
