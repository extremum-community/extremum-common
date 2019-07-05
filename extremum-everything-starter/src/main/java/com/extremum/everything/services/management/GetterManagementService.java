package com.extremum.everything.services.management;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;

public interface GetterManagementService {
    ResponseDto get(Descriptor id, boolean expand);
}
