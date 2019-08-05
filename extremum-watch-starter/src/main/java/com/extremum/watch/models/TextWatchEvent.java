package com.extremum.watch.models;

import com.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Collection;

import static com.extremum.watch.models.TextWatchEvent.COLLECTION_NAME;


@Getter
@Setter
@RequiredArgsConstructor
@Document(COLLECTION_NAME)
public class TextWatchEvent {
    /**
     * Collection name of the all child's of this class
     */
    public static final String COLLECTION_NAME = "watch_events";

    @Id
    private ObjectId id;

    @CreatedDate
    @Indexed
    private ZonedDateTime created;

    @Version
    private long version;

    private final String operationType;
    private final String jsonPatch;
    private final String modelId;

    public TextWatchEventNotificationDto toDto(Collection<String> subscribers) {
        return new TextWatchEventNotificationDto(operationType, jsonPatch, subscribers);
    }

}
