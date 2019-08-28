package io.extremum.sharedmodels.descriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.extremum.sharedmodels.annotation.UsesStaticDependencies;
import io.extremum.sharedmodels.content.Display;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Builder
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Descriptor implements Serializable {
    public static final String COLLECTION = "descriptor-identifiers";

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("type")
    private Type type;

    @JsonProperty("internalId")
    private String internalId;
    @JsonProperty("modelType")
    private String modelType;
    @JsonProperty("storageType")
    private StorageType storageType;

    @JsonProperty("collection")
    private CollectionDescriptor collection;

    @JsonProperty("created")
    private ZonedDateTime created;
    @JsonProperty("modified")
    private ZonedDateTime modified;
    @JsonProperty("version")
    private Long version;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("display")
    private Display display;

    public Descriptor() {
    }

    public Descriptor(String externalId) {
        this.externalId = externalId;
    }

    public static Descriptor forCollection(String externalId, CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = new Descriptor(externalId);
        descriptor.type = Type.COLLECTION;
        descriptor.collection = collectionDescriptor;
        return descriptor;
    }

    public String getExternalId() {
        if (this.externalId == null) {
            fillByInternalId();
        }
        return this.externalId;
    }

    public Type effectiveType() {
        if (type == null) {
            return Type.SINGLE;
        }
        return type;
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

    private void fillByIds() {
        if (this.internalId != null) {
            fillByInternalId();
        } else if (this.externalId != null) {
            fillByExternalId();
        }
    }

    @UsesStaticDependencies
    private void fillByInternalId() {
        //noinspection deprecation
        StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByInternalId(internalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.externalId != null)
                .orElseThrow(() -> new DescriptorNotFoundException(
                                String.format("Internal id %s without corresponding descriptor", internalId)
                        )
                );
    }

    @UsesStaticDependencies
    private void fillByExternalId() {
        //noinspection deprecation
        StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByExternalId(this.externalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.internalId != null)
                .orElseThrow(() -> new DescriptorNotFoundException(
                        "Internal ID was not found for external ID " + this.externalId)
                );
    }


    private Descriptor copyFieldsFromAnotherDescriptor(Descriptor d) {
        this.externalId = d.externalId;
        this.internalId = d.internalId;
        this.modelType = d.modelType;
        this.storageType = d.storageType;
        this.created = d.created;
        this.modified = d.modified;
        this.version = d.version;
        this.deleted = d.deleted;
        this.display = d.display;

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

    @JsonIgnore
    public boolean isSingle() {
        return effectiveType() == Type.SINGLE;
    }

    @JsonIgnore
    public boolean isCollection() {
        return effectiveType() == Type.COLLECTION;
    }

    public enum Type {
        SINGLE("single"),
        COLLECTION("collection");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Type fromString(String value) {
            if (value == null) {
                return SINGLE;
            }

            for (Type type : Type.values()) {
                if (type.getValue().equalsIgnoreCase(value)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(String.format("'%s' is not a known descriptor type", value));
        }
    }

    public enum StorageType {
        MONGO("mongo"),
        ELASTICSEARCH("elastic"),
        POSTGRES("postgres");

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

    public enum FIELDS {
        externalId, internalId, modelType, storageType, created, modified, version, deleted, display
    }
}


