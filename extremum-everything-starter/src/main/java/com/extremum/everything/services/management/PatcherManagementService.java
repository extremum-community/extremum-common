package com.extremum.everything.services.management;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;

public interface PatcherManagementService {
    ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand);
}
