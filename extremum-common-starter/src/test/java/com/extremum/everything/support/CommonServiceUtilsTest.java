package com.extremum.everything.support;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
class CommonServiceUtilsTest {
    private static final MongoCommonDao<A> NOT_USED = null;

    private final AService aService = new AService(NOT_USED);

    @Test
    void givenCommonServiceIsOfTypeA_whenCheckingForA_thenShouldReturnTrue() {
        assertTrue(CommonServiceUtils.isCommonServiceOfModelClass(aService, A.class));
    }

    @Test
    void givenCommonServiceIsOfTypeA_whenCheckingForB_thenShouldReturnFalse() {
        assertFalse(CommonServiceUtils.isCommonServiceOfModelClass(aService, B.class));
    }

    @Test
    void givenCommonServiceIsOfTypeA_whenFindServiceModelClass_thenReturnClassA() {
        Class<?> modelClass = CommonServiceUtils.findServiceModelClass(aService);
        assertThat(modelClass, is(sameInstance(A.class)));
    }

    private static class A extends MongoCommonModel {
    }

    private static class B extends MongoCommonModel {
    }

    private static class AService extends MongoCommonServiceImpl<A> {
        AService(MongoCommonDao<A> dao) {
            super(dao);
        }
    }
}