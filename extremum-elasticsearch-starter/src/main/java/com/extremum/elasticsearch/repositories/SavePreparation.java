package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
class SavePreparation {
    void prepareForSave(Object object) {
        if (object == null) {
            return;
        }

        if (!(object instanceof ElasticsearchCommonModel)) {
            return;
        }

        ElasticsearchCommonModel model = (ElasticsearchCommonModel) object;
        if (model.getCreated() == null) {
            model.setCreated(ZonedDateTime.now());
        }
        if (model.getModified() == null) {
            model.setModified(ZonedDateTime.now());
        }
        if (model.getDeleted() == null) {
            model.setDeleted(false);
        }
    }
}
