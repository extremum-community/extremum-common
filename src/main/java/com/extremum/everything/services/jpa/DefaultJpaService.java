package com.extremum.everything.services.jpa;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.services.EverythingEverythingService;
import org.springframework.core.ResolvableType;

import java.util.List;

public interface DefaultJpaService<M extends PostgresCommonModel> extends EverythingEverythingService {
    @Override
    default boolean isSupportedModel(Class<? extends Model> modelClass) {
        return PostgresCommonModel.class.isAssignableFrom(modelClass);
    }

    default PostgresCommonService<? extends M> findServiceByModel(List<PostgresCommonService<? extends M>> services, Class<? extends PostgresCommonModel> modelClass) {
        ResolvableType type = ResolvableType.forClassWithGenerics(PostgresCommonService.class, modelClass);
        return services.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot found implementation for PostgresCommonService for model " + modelClass));
    }

    default M getById(List<PostgresCommonService<? extends M>> services, String internalId) {
        return findServiceByModel(services, getModelByDescriptorId(internalId)).get(internalId);
    }

    @SuppressWarnings("unchecked")
    default Class<? extends PostgresCommonModel> getModelByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

//      cast is safe because filtering this service in management service by method isSupportedModel
        return (Class<? extends PostgresCommonModel>) ModelClasses.getClassByModelName(descriptor.getModelType());
    }
}
