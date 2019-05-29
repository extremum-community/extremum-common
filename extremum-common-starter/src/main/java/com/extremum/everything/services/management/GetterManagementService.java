package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;

public interface GetterManagementService {
    ResponseDto get(Descriptor id, boolean expand);
}
