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

@Getter
@Setter
@EntityListeners({JpaCommonModelLifecycleListener.class, AuditingEntityListener.class})
@MappedSuperclass
public abstract class PostgresCommonModel implements PersistableCommonModel<UUID> {
    @Transient
    private Descriptor uuid;

    @Id
    @Column(name = "id")
    private UUID id;

    @CreatedDate
    @Column(name = "created")
    private ZonedDateTime created;

    @LastModifiedDate
    @Column(name = "modified")
    private ZonedDateTime modified;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "deleted")
    private Boolean deleted = false;
}
