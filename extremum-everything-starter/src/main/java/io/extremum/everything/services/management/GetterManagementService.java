package io.extremum.everything.services.management;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;

public interface GetterManagementService {
    ResponseDto get(Descriptor id, boolean expand);
}
