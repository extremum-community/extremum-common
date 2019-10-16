package io.extremum.sharedmodels.converter;

import io.extremum.sharedmodels.dto.Projection;
import io.extremum.sharedmodels.proto.common.ProtoProjection;
import io.extremum.sharedmodels.proto.everything.ProtoEvrEvrGetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoEvrEvrGetRequestConverter {
    private final ProtoProjectionConverter projectionConverter;

    public String extractId(ProtoEvrEvrGetRequest request) {
        return request.getId().equals("") ? null : request.getId();
    }

    public Projection extractProjection(ProtoEvrEvrGetRequest request) {
        return projectionConverter.createFromProto(request.getProjection());
    }

    public boolean extractExpand(ProtoEvrEvrGetRequest request) {
        return request.getExpand();
    }

    public ProtoEvrEvrGetRequest createProto(String id, ProtoProjection projection, boolean expand) {
        return ProtoEvrEvrGetRequest.newBuilder()
                .setId(id)
                .setProjection(projection)
                .setExpand(expand)
                .build();
    }
}
