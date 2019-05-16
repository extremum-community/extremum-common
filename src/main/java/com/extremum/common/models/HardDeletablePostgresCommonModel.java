package com.extremum.common.models;

import lombok.Setter;

import javax.persistence.MappedSuperclass;

@Setter
@MappedSuperclass
public abstract class HardDeletablePostgresCommonModel extends PostgresCommonModel {
}
