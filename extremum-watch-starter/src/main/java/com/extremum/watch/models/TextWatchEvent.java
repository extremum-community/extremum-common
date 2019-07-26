package com.extremum.watch.models;

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

import static com.extremum.watch.models.TextWatchEvent.COLLECTION_NAME;


@RequiredArgsConstructor
@Getter
@Setter
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
    private final String patch;

    public TextWatchEventDto toDto() {
        return new TextWatchEventDto(operationType, patch);
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class TextWatchEventDto {
        private final String operationType;
        private final String updateBody;
    }
}
