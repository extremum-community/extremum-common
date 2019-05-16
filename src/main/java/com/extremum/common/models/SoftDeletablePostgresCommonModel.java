package com.extremum.common.models;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Setter
@MappedSuperclass
public abstract class SoftDeletablePostgresCommonModel extends PostgresCommonModel {
    @Override
    @Column
    public Boolean getDeleted() {
        return super.getDeleted();
    }
}
