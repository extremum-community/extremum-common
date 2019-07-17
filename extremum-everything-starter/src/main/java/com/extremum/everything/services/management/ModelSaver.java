package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.services.SaverService;
import com.extremum.everything.services.defaultservices.DefaultSaver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author rpuch
 */
@Slf4j
@RequiredArgsConstructor
public class ModelSaver {
    private final List<SaverService<? extends Model>> saverServices;
    private final DefaultSaver<? extends Model> defaultSaver;

    public Model saveModel(Model model) {
        String modelName = ModelUtils.getModelName(model);
        Saver<Model> saver = findSaver(modelName);
        return saver.save(model);
    }

    private Saver<Model> findSaver(String modelName) {
        SaverService<? extends Model> service = EverythingServices.findServiceForModel(modelName, saverServices);
        if (service != null) {
            return (Saver<Model>) new NonDefaultSaver<>(service);
        }

        return (Saver<Model>) defaultSaver;
    }
}
