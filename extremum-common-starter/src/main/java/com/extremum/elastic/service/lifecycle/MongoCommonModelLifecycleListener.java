package com.extremum.elastic.service.lifecycle;

import com.extremum.common.models.MongoCommonModel;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * @author rpuch
 */
public class MongoCommonModelLifecycleListener extends AbstractMongoEventListener<MongoCommonModel> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<MongoCommonModel> event) {
        super.onBeforeConvert(event);

        MongoCommonModel model = event.getSource();

        model.fillRequiredFields();
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MongoCommonModel> event) {
        super.onAfterSave(event);
        
        MongoCommonModel model = event.getSource();

        model.createDescriptorIfNeeded();
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<MongoCommonModel> event) {
        super.onAfterConvert(event);

        MongoCommonModel model = event.getSource();

        model.resolveDescriptor();
    }
}
