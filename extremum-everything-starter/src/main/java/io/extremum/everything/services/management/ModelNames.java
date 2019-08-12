package io.extremum.everything.services.management;

import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author rpuch
 */
@Slf4j
class ModelNames {
    String determineModelName(Descriptor id) {
        requireNonNull(id, "ID can't be null");

        String modelName = determineModelNameById(id);
        if (modelName == null) {
            log.error("Unable to determine a model name for id {}", id);
            throw new EverythingEverythingException(format("Can't determine a model name for the ID '%s'", id));
        } else {
            log.debug("Model name for id {} is {}", id, modelName);
            return modelName;
        }
    }

    private String determineModelNameById(Descriptor id) {
        requireNonNull(id, "ID can't be null");
        return id.getModelType();
    }
}
