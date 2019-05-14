package com.extremum.common.dao.impl;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataJpaCommonDao<M extends PostgresCommonModel>
        extends PostgresCommonDao<M>, JpaRepository<M, UUID> {
}