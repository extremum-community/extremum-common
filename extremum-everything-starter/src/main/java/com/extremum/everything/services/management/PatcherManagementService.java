package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;

public interface PatcherManagementService {
    ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand);
}
