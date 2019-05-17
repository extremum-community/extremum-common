package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.service.lifecycle.JpaCommonModelLifecycleListener;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@EntityListeners({JpaCommonModelLifecycleListener.class, AuditingEntityListener.class})
@MappedSuperclass
public abstract class PostgresCommonModel implements PersistableCommonModel<UUID> {
    @Getter(onMethod_ = {@Transient})
    private Descriptor uuid;

    private UUID id;
    @Getter(onMethod_ = {@CreatedDate})
    private ZonedDateTime created;
    @Getter(onMethod_ = {@LastModifiedDate})
    private ZonedDateTime modified;
    @Getter(onMethod_ = {@Version})
    private Long version;
    @Getter(onMethod_ = {@Transient})
    private Boolean deleted = false;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
    })
    public UUID getId() {
        return id;
    }
}
