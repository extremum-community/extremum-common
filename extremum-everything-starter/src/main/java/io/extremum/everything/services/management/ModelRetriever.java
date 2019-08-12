package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.defaultservices.DefaultGetter;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.lang.String.format;

/**
 * @author rpuch
 */
@Slf4j
@RequiredArgsConstructor
public class ModelRetriever {
    private final List<GetterService<?>> getterServices;
    private final DefaultGetter defaultGetter;

    private final ModelNames modelNames = new ModelNames();

    public Model retrieveModel(Descriptor id) {
        String modelName = modelNames.determineModelName(id);
        Getter getter = findGetter(modelName);
        Model model = getter.get(id.getInternalId());

        if (model != null) {
            log.debug(format("Model with ID '%s' was found by service '%s': '%s'", id, getter, model));
        } else {
            log.debug(format("Model with ID '%s' wasn't found by service '%s'", id, getter));
        }
        return model;
    }

    private Getter findGetter(String modelName) {
        GetterService<? extends Model> service = ModelServices.findServiceForModel(modelName, getterServices);
        if (service != null) {
            return new NonDefaultGetter(service);
        }

        return defaultGetter;
    }
}
