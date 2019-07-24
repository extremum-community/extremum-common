package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.security.EverythingRoleSecurity;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleSecurityEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private final EverythingEverythingManagementService everythingService;
    private final EverythingRoleSecurity roleSecurity;

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        roleSecurity.checkGetAllowed(id);

        return everythingService.get(id, expand);
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        roleSecurity.checkPatchAllowed(id);

        return everythingService.patch(id, patch, expand);
    }

    @Override
    public void remove(Descriptor id) {
        roleSecurity.checkRemovalAllowed(id);

        everythingService.remove(id);
    }

    @Override
    public CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id,
            Projection projection, boolean expand) {
        return everythingService.fetchCollection(id, projection, expand);
    }
}
