package com.extremum.jpa.dao.impl;

import com.extremum.common.models.BasicModel;
import com.extremum.jpa.dao.PostgresCommonDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * @author rpuch
 */
@NoRepositoryBean
public interface SpringDataJpaCommonDao<M extends BasicModel<UUID>>
        extends PostgresCommonDao<M>, JpaRepository<M, UUID> {
}
