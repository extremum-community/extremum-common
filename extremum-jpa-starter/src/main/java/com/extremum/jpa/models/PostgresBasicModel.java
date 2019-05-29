package com.extremum.jpa.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.BasicModel;
import com.extremum.jpa.services.lifecycle.JpaCommonModelLifecycleListener;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.UUID;

@Setter
@EntityListeners(JpaCommonModelLifecycleListener.class)
@MappedSuperclass
public abstract class PostgresBasicModel implements BasicModel<UUID> {
    @Getter(onMethod_ = {@Transient})
    private Descriptor uuid;

    private UUID id;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    public UUID getId() {
        return id;
    }
}
