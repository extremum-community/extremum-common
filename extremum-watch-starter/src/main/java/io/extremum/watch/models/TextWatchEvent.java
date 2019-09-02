package io.extremum.watch.models;

import io.extremum.common.model.Model;
import io.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;

import static io.extremum.watch.models.TextWatchEvent.COLLECTION_NAME;


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

    private String jsonPatch;
    private String modelId;
    private ModelMetadata modelMetadata;
    private Set<String> subscribers;

    public TextWatchEvent() {
    }

    public TextWatchEvent(String jsonPatch, String modelId, Model targetModel) {
        this.jsonPatch = jsonPatch;
        this.modelId = modelId;
        modelMetadata = ModelMetadata.fromModel(targetModel);
    }

    public TextWatchEventNotificationDto toDto(Collection<String> subscribers) {
        return new TextWatchEventNotificationDto(jsonPatch, subscribers);
    }

    public void touchModelMotificationTime() {
        modelMetadata.setModified(ZonedDateTime.now());
    }
}
