package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleSecurityEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private final EverythingEverythingManagementService everythingService;
    private final RoleSecurity roleSecurity;

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
