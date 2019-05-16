package com.extremum.common.repository.jpa;

import com.extremum.common.models.SoftDeletablePostgresCommonModel;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;

import javax.persistence.EntityManager;

/**
 * @author rpuch
 */
public class ExtremumJpaRepositoryFactory extends JpaRepositoryFactory {
    public ExtremumJpaRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (supportsSoftDeletion(metadata.getDomainType())) {
            return BaseJpaRepository.class;
        } else {
            return SimpleJpaRepository.class;
        }
    }

    private boolean supportsSoftDeletion(Class<?> domainType) {
        return SoftDeletablePostgresCommonModel.class.isAssignableFrom(domainType);
    }
}
