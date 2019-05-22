package com.extremum.common.repository.jpa;

import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.models.SoftDeletePostgresModel;
import models.HardDeleteJpaModel;
import models.TestJpaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
public class JpaSoftDeletionTest {
    private final JpaSoftDeletion softDeletion = new JpaSoftDeletion();

    @Test
    public void testStandardClasses() {
        assertTrue(softDeletion.supportsSoftDeletion(TestJpaModel.class));
        assertTrue(softDeletion.supportsSoftDeletion(SoftDeletePostgresModel.class));
        assertFalse(softDeletion.supportsSoftDeletion(HardDeleteJpaModel.class));
        assertFalse(softDeletion.supportsSoftDeletion(PostgresCommonModel.class));
    }

    @Test
    public void givenGetDeletedIsOverridenWithoutAnnotations_whenCheckingSoftDeletionSupport_thenItShouldBeSupported() {
        assertTrue(softDeletion.supportsSoftDeletion(GetDeletedOverridenWithoutAnnotations.class));
    }

    private static class GetDeletedOverridenWithoutAnnotations extends PostgresCommonModel {
        @Override
        public Boolean getDeleted() {
            return super.getDeleted();
        }
    }
}