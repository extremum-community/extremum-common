package com.extremum.everything.services;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.PersistableCommonModel;

public interface MergeServiceFieldMixin {
    default <T extends BasicModel> void mergeServiceFields(T from, T to) {
        to.setId(from.getId());
        to.setUuid(from.getUuid());
        // TODO: the following is very ugly, refactor
        if ((from instanceof PersistableCommonModel) && (to instanceof PersistableCommonModel)) {
            PersistableCommonModel persistableFrom = (PersistableCommonModel) from;
            PersistableCommonModel persistableTo = (PersistableCommonModel) to;
            persistableTo.setVersion(persistableFrom.getVersion());
            persistableTo.setDeleted(persistableFrom.getDeleted());
            persistableTo.setCreated(persistableFrom.getCreated());
            persistableTo.setModified(persistableFrom.getModified());
        }
    }
}
