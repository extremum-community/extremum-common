package com.extremum.common.dao.extractor;

import com.extremum.common.models.ElasticData;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.DateUtils;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class Extractor {
    public abstract ElasticData extract();
    public abstract List<ElasticData> extractAsList();

    protected void populateFromSourceMap(Map<String, Object> sourceMap, ElasticData.ElasticDataBuilder builder) {
        ofNullable(sourceMap.get(PersistableCommonModel.FIELDS.created.name()))
                .map(d -> DateUtils.parseZonedDateTime((String) d, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .ifPresent(builder::created);

        ofNullable(sourceMap.get(PersistableCommonModel.FIELDS.modified.name()))
                .map(d -> DateUtils.parseZonedDateTime((String) d, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .ifPresent(builder::modified);

        ofNullable(sourceMap.get(PersistableCommonModel.FIELDS.deleted.name()))
                .map(Boolean.class::cast)
                .ifPresent(builder::deleted);
    }
}
