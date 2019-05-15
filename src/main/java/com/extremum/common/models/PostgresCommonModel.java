package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.service.lifecycle.JpaCommonModelLifecycleListener;
import lombok.Getter;
import lombok.Setter;
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

    @Getter(onMethod_ = {@Id, @GeneratedValue, @Column(name = "id")})
    private UUID id;

    @Getter(onMethod_ = {@CreatedDate, @Column(name = "created")})
    private ZonedDateTime created;

    @Getter(onMethod_ = {@LastModifiedDate, @Column(name = "modified")})
    private ZonedDateTime modified;

    @Getter(onMethod_ = {@Version, @Column(name = "version")})
    private Long version;

    @Getter(onMethod_ = {@Column(name = "deleted")})
    private Boolean deleted = false;
}
