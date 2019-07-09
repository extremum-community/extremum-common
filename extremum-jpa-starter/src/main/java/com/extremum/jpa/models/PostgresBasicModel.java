package com.extremum.jpa.models;

import com.extremum.common.models.BasicModel;
import com.extremum.jpa.services.lifecycle.JpaCommonModelLifecycleListener;
import com.extremum.sharedmodels.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.UUID;

@Setter
@EntityListeners(JpaCommonModelLifecycleListener.class)
@MappedSuperclass
public abstract class PostgresBasicModel implements BasicModel<UUID> {
    @Getter(onMethod_ = {@Transient})
    private Descriptor uuid;

    private UUID id;

    @Id
    public UUID getId() {
        return id;
    }
}
