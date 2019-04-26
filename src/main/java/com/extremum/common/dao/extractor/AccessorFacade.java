package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.DateUtils;

import java.time.ZonedDateTime;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class AccessorFacade {
    public abstract String getId();

    public abstract Descriptor getUuid();

    public abstract Long getVersion();

    public abstract String getRawSource();

    public abstract Long getSeqNo();

    public abstract Long getPrimaryTerm();

    public abstract Map<String, Object> getSourceAsMap();

    public Boolean getDeleted() {
        return ofNullable(getSourceAsMap())
                .map(m -> m.get(PersistableCommonModel.FIELDS.deleted.name()))
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);
    }

    public ZonedDateTime getCreated() {
        return stringToZonedDateTime(PersistableCommonModel.FIELDS.created);
    }

    public ZonedDateTime getModified() {
        return stringToZonedDateTime(PersistableCommonModel.FIELDS.modified);
    }

    protected ZonedDateTime stringToZonedDateTime(PersistableCommonModel.FIELDS field) {
        return ofNullable(getSourceAsMap())
                .map(m -> m.get(field.name()))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(v -> DateUtils.parseZonedDateTime(v, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .orElse(null);
    }
}
