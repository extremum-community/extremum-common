package com.extremum.common.service;

import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.models.MongoCommonModel;
import org.springframework.data.mongodb.core.mapping.event.*;

/**
 * @author rpuch
 */
public class MongoCommonModelLifecycleListener extends AbstractMongoEventListener<MongoCommonModel> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<MongoCommonModel> event) {
        super.onBeforeConvert(event);

        MongoCommonModel model = event.getSource();

        if (model.getId() == null && model.getUuid() != null) {
            model.setId(MongoDescriptorFactory.resolve(model.getUuid()));
        }
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MongoCommonModel> event) {
        super.onAfterSave(event);
        
        MongoCommonModel model = event.getSource();
        
        if (model.getUuid() == null) {
            model.setUuid(MongoDescriptorFactory.create(model.getId(), model.getModelName()));
        }
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<MongoCommonModel> event) {
        super.onAfterConvert(event);

        MongoCommonModel model = event.getSource();

        model.setUuid(MongoDescriptorFactory.fromInternalId(model.getId()));
    }
}
