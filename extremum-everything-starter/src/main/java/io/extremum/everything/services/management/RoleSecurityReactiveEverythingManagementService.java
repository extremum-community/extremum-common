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
        return Mono.fromRunnable(() -> roleSecurity.checkGetAllowed(id))
                .then(everythingService.get(id, expand));
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return Mono.fromRunnable(() -> roleSecurity.checkPatchAllowed(id))
                .then(everythingService.patch(id, patch, expand));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return Mono.fromRunnable(() -> roleSecurity.checkRemovalAllowed(id))
                .then(everythingService.remove(id));
    }
}
