package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.Descriptor;

import java.time.ZonedDateTime;

public interface AccessorFacade {
    String getId();

    Descriptor getUuid();

    Long getVersion();

    Boolean getDeleted();

    ZonedDateTime getCreated();

    ZonedDateTime getModified();

    String getRawSource();

    Long getSeqNo();

    Long getPrimaryTerm();
}
