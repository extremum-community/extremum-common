package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.everything.config.listener.ModelClasses;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author rpuch
 */
public class ConstantModelClasses implements ModelClasses {
    private final Map<String, Class<? extends Model>> modelNameToClassMap;

    public ConstantModelClasses(Map<String, Class<? extends Model>> modelNameToClassMap) {
        this.modelNameToClassMap = ImmutableMap.copyOf(modelNameToClassMap);
    }

    @Override
    public Class<? extends Model> getClassByModelName(String modelName) {
        return modelNameToClassMap.get(modelName);
    }

    @Override
    public Class<? extends Model> getModelByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return this.getClassByModelName(descriptor.getModelType());
    }
}
