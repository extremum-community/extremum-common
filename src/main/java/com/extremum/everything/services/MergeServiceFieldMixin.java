package com.extremum.everything.services;

import com.extremum.common.models.PersistableCommonModel;

public interface MergeServiceFieldMixin {
    default <T extends PersistableCommonModel> void mergeServiceFields(T from, T to) {
        to.setId(from.getId());
        to.setUuid(from.getUuid());
        to.setVersion(from.getVersion());
        to.setCreated(from.getCreated());
        to.setDeleted(from.getDeleted());
    }
}
