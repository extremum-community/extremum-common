package io.extremum.sharedmodels.converter;

import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.dto.Projection;
import io.extremum.sharedmodels.proto.common.ProtoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoProjectionConverter {
    private final ProtoZonedTimestampConverter timestampConverter;

    public ProtoProjection createProto(Projection projection) {
        if (projection == null) {
            return ProtoProjection.getDefaultInstance();
        }

        ProtoProjection.Builder protoBuilder = ProtoProjection.newBuilder();

        if (projection.getSince() != null) {
            protoBuilder.setSince(timestampConverter.createProto(projection.getSince()));
        }

        if (projection.getUntil() != null) {
            protoBuilder.setUntil(timestampConverter.createProto(projection.getUntil()));
        }

        if (projection.getLimit() != null) {
            protoBuilder.setLimit(Int32Value.newBuilder().setValue(projection.getLimit()).build());
        }
        if (projection.getOffset() != null) {
            protoBuilder.setOffset(Int32Value.newBuilder().setValue(projection.getOffset()).build());
        }
        return protoBuilder.build();
    }

    public Projection createFromProto(ProtoProjection proto) {
        if (proto == null || ProtoProjection.getDefaultInstance().equals(proto)) {
            return null;
        } else {
            Projection projection = new Projection();
            projection.setSince(timestampConverter.createFromProto(proto.getSince()));
            projection.setUntil(timestampConverter.createFromProto(proto.getUntil()));
            if (proto.hasLimit()) {
                projection.setLimit(proto.getLimit().getValue());
            }
            if (proto.hasOffset()) {
                projection.setOffset(proto.getOffset().getValue());
            }
            return projection;
        }
    }
}
