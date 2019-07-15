package com.extremum.everything.services.management;

import com.extremum.everything.services.EverythingEverythingService;

import java.util.Collection;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class EverythingServices {
    public static <T extends EverythingEverythingService> T findServiceForModel(String modelName,
            Collection<? extends T> services) {
        requireNonNull(modelName, "Name of a model can't be null");
        requireNonNull(services, "Services list can't be null");

        return services.stream()
                .filter(getIsServiceSupportsModelFilter(modelName))
                .findAny()
                .orElse(null);
    }

    private static Predicate<? super EverythingEverythingService> getIsServiceSupportsModelFilter(String modelName) {
        return service -> modelName.equals(service.getSupportedModel());
    }

    private EverythingServices() {}
}
