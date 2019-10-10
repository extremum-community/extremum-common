package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RoleSecurityReactiveEverythingManagementService implements ReactiveEverythingManagementService {
    private final ReactiveEverythingManagementService everythingService;
    private final RoleSecurity roleSecurity;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        roleSecurity.checkGetAllowed(id);

        return everythingService.get(id, expand);
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        roleSecurity.checkPatchAllowed(id);

        return everythingService.patch(id, patch, expand);
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        roleSecurity.checkRemovalAllowed(id);

        return everythingService.remove(id);
    }
}
