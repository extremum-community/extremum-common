package io.extremum.sharedmodels.converter;

import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.proto.common.ZonedTimestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ProtoZonedTimestampConverter {

    public ZonedTimestamp createProto(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return ZonedTimestamp.getDefaultInstance();
        }

        Instant dateTimeInstant = dateTime.toInstant();
        return ZonedTimestamp.newBuilder()
                .setZoneId(dateTime.getZone().getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(dateTimeInstant.getEpochSecond())
                        .setNanos(dateTimeInstant.getNano())
                        .build())
                .build();
    }

    public ZonedDateTime createFromProto(ZonedTimestamp timestamp) {
        if (timestamp.equals(ZonedTimestamp.getDefaultInstance())) {
            return null;
        }
        return Instant
                .ofEpochSecond(timestamp.getTimestamp().getSeconds(),
                        timestamp.getTimestamp().getNanos())
                .atZone(ZoneId.of(timestamp.getZoneId()));
    }
}
