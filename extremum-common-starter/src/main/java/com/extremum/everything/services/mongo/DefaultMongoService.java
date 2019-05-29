package com.extremum.everything.services.mongo;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.elastic.service.MongoCommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.services.EverythingEverythingService;
import org.springframework.core.ResolvableType;

import java.util.List;

public interface DefaultMongoService<M extends MongoCommonModel> extends EverythingEverythingService {
    default MongoCommonService<? extends M> findServiceByModel(List<MongoCommonService<? extends M>> services, Class<? extends MongoCommonModel> modelClass) {
        ResolvableType type = ResolvableType.forClassWithGenerics(MongoCommonService.class, modelClass);
        return services.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot found implementation for MongoCommonService for model " + modelClass));
    }

    default M getById(List<MongoCommonService<? extends M>> services, String internalId) {
        return findServiceByModel(services, getModelByDescriptorId(internalId)).get(internalId);
    }

    @Override
    default boolean isSupportedModel(Class<? extends Model> modelClass) {
        return MongoCommonModel.class.isAssignableFrom(modelClass);
    }

    @SuppressWarnings("unchecked")
    default Class<? extends MongoCommonModel> getModelByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

//      cast is safe because filtering this service in management service by method isSupportedModel
        return (Class<? extends MongoCommonModel>) ModelClasses.getClassByModelName(descriptor.getModelType());
    }
}
