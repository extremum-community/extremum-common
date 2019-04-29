package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.Descriptor;

import java.util.Map;

public abstract class AccessorFacade {
    public abstract String getId();

    public abstract Descriptor getUuid();

    public abstract Long getVersion();

    public abstract Long getSeqNo();

    public abstract Long getPrimaryTerm();

    public abstract String getRawSource();

    public abstract Map<String, Object> getSourceAsMap();
}
