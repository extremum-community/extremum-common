package io.extremum.everything.services.management;

import io.extremum.common.response.Response;
import io.extremum.everything.collection.Projection;
import io.extremum.sharedmodels.descriptor.Descriptor;

public interface EverythingMultiplexer {
    Response get(Descriptor id, Projection projection, boolean expand);
}
