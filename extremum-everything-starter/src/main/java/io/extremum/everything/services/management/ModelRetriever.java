package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.GetterService;
import io.extremum.everything.services.ReactiveGetterService;
import io.extremum.everything.services.defaultservices.DefaultGetter;
import io.extremum.everything.services.defaultservices.DefaultReactiveGetter;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.lang.String.format;

/**
 * @author rpuch
 */
@Slf4j
@RequiredArgsConstructor
public class ModelRetriever {
    private final List<GetterService<?>> getterServices;
    private final List<ReactiveGetterService<?>> reactiveGetterServices;
    private final DefaultGetter defaultGetter;
    private final DefaultReactiveGetter defaultReactiveGetter;

    private final ModelNames modelNames = new ModelNames();

    public Model retrieveModel(Descriptor id) {
        Getter getter = findGetter(id);
        Model model = getter.get(id.getInternalId());

        logModel(id, getter, model);

        return model;
    }

    private Getter findGetter(Descriptor id) {
        String modelName = modelNames.determineModelName(id);
        GetterService<? extends Model> service = ModelServices.findServiceForModel(modelName, getterServices);
        if (service != null) {
            @SuppressWarnings("unchecked") GetterService<Model> castService = (GetterService<Model>) service;
            return new NonDefaultGetter(castService);
        }

        return defaultGetter;
    }

    private void logModel(Descriptor id, Object getter, Model model) {
        if (log.isDebugEnabled()) {
            if (model != null) {
                log.debug(format("Model with ID '%s' was found by service '%s': '%s'", id, getter, model));
            } else {
                log.debug(format("Model with ID '%s' wasn't found by service '%s'", id, getter));
            }
        }
    }

    public Mono<Model> retrieveModelReactively(Descriptor id) {
        ReactiveGetter getter = findReactiveGetter(id);
        return getter.reactiveGet(id.getInternalId())
                .doOnNext(model -> logModel(id, getter, model));
    }

    private ReactiveGetter findReactiveGetter(Descriptor id) {
        String modelName = modelNames.determineModelName(id);
        ReactiveGetterService<? extends Model> service = ModelServices.findServiceForModel(modelName,
                reactiveGetterServices);
        if (service != null) {
            @SuppressWarnings("unchecked") ReactiveGetterService<Model> castService = (ReactiveGetterService<Model>) service;
            return new NonDefaultReactiveGetter(castService);
        }

        return defaultReactiveGetter;
    }
}
