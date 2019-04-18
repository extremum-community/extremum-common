package com.extremum.common.descriptor;

import com.extremum.common.converters.MongoZonedDateTimeConverter;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.mongodb.morphia.annotations.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Builder
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(value = "descriptor-identifiers", noClassnameStored = true)
@Converters({MongoZonedDateTimeConverter.class})
public class Descriptor implements Serializable {
    @Id
    @JsonProperty("externalId")
    private String externalId;
    @Indexed
    @JsonProperty("internalId")
    private String internalId;
    @JsonProperty("modelType")
    private String modelType;
    @JsonProperty("storageType")
    private StorageType storageType;

    @Property
    @JsonProperty("created")
    private ZonedDateTime created;
    @Property
    @JsonProperty("modified")
    private ZonedDateTime modified;
    @Version
    @JsonProperty("version")
    private Long version;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("display")
    private String display;

    public Descriptor() {
    }

    public Descriptor(String externalId) {
        this.externalId = externalId;
    }

    @PrePersist
    public void fillRequiredFields() {
        initCreated();
        initModified();
    }

    private void initCreated() {
        if (this.created == null) {
            this.created = ZonedDateTime.now();
        }
    }

    private void initModified() {
        this.modified = ZonedDateTime.now();
    }

    public String getExternalId() {
        if (this.externalId == null) {
            fillByInternalId();
        }
        return this.externalId;
    }

    public String getInternalId() {
        if (this.internalId == null) {
            fillByExternalId();
        }
        return this.internalId;
    }

    public StorageType getStorageType() {
        if (this.storageType == null) {
            fillByIds();
            if (this.storageType == null) {
                throw new IllegalStateException(
                        String.format("StorageType can't be resolved by both id's: internalId - %s; descriptorId %s",
                                internalId, externalId)
                );
            }
        }
        return this.storageType;
    }

    public String getModelType() {
        if (this.modelType == null) {
            fillByIds();
        }
        return this.modelType;
    }

    public String getDisplay() {
        return display;
    }

    private void fillByIds() {
        if (this.internalId != null) {
            fillByInternalId();
        } else if (this.externalId != null) {
            fillByExternalId();
        }
    }

    private void fillByInternalId() {
        DescriptorService.loadByInternalId(internalId)
                .map(this::copy)
                .filter(d -> d.externalId != null)
                .orElseThrow(() -> new IllegalStateException(
                                String.format("Internal id %s without corresponding descriptor", internalId)
                        )
                );
    }

    private void fillByExternalId() {
        DescriptorService.loadByExternalId(this.externalId)
                .map(this::copy)
                .filter(d -> d.internalId != null)
                .orElseThrow(() -> new DescriptorNotFoundException("Internal ID was not found for external ID " + this.externalId));
    }


    private Descriptor copy(Descriptor d) {
        this.externalId = d.externalId;
        this.internalId = d.internalId;
        this.modelType = d.modelType;
        this.storageType = d.storageType;

        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Descriptor that = (Descriptor) o;
        return Objects.equals(getExternalId(), that.getExternalId()) &&
                Objects.equals(getInternalId(), that.getInternalId()) &&
                Objects.equals(getModelType(), that.getModelType()) &&
                getStorageType() == that.getStorageType();
    }

    @Override
    public int hashCode() {
        return this.getExternalId().hashCode();
    }

    @Override
    public String toString() {
        return this.getExternalId();
    }


    public enum StorageType {
        MONGO("mongo"),
        ELASTIC("elastic"),
        POSTGRE("postgre");

        private final String value;

        StorageType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static StorageType fromString(String value) {
            if (value != null) {
                for (StorageType type : StorageType.values()) {
                    if (type.getValue().equalsIgnoreCase(value)) {
                        return type;
                    }
                }
            }

            return null;
        }
    }
}


