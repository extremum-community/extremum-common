package com.extremum.common.repository.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;

import javax.persistence.EntityManager;

/**
 * @author rpuch
 */
public class ExtremumJpaRepositoryFactory extends JpaRepositoryFactory {
    private final JpaSoftDeletion jpaSoftDeletion = new JpaSoftDeletion();

    public ExtremumJpaRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (jpaSoftDeletion.supportsSoftDeletion(metadata.getDomainType())) {
            return BaseJpaRepository.class;
        } else {
            return SimpleJpaRepository.class;
        }
    }
}
