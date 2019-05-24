package com.extremum.everything.services.jpa;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.service.PostgresBasicService;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.services.EverythingEverythingService;
import org.springframework.core.ResolvableType;

import java.util.List;

public interface DefaultJpaService<M extends PostgresBasicModel> extends EverythingEverythingService {
    @Override
    default boolean isSupportedModel(Class<? extends Model> modelClass) {
        return PostgresBasicModel.class.isAssignableFrom(modelClass);
    }

    default PostgresBasicService<? extends M> findServiceByModel(List<PostgresBasicService<? extends M>> services,
            Class<? extends PostgresBasicModel> modelClass) {
        ResolvableType type = ResolvableType.forClassWithGenerics(PostgresBasicService.class, modelClass);
        return services.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot found implementation of PostgresBasicService for model " + modelClass));
    }

    default M getById(List<PostgresBasicService<? extends M>> services, String internalId) {
        return findServiceByModel(services, getModelByDescriptorId(internalId)).get(internalId);
    }

    @SuppressWarnings("unchecked")
    default Class<? extends PostgresBasicModel> getModelByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

//      cast is safe because filtering this service in management service by method isSupportedModel
        return (Class<? extends PostgresBasicModel>) ModelClasses.getClassByModelName(descriptor.getModelType());
    }
}
