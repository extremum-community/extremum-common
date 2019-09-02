package io.extremum.common.support;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class CommonServiceUtilsTest {
    private static final MongoCommonDao<A> NOT_USED = null;

    private final AService aService = new AService(NOT_USED);

    @Test
    void givenCommonServiceIsOfTypeA_whenFindServiceModelClass_thenReturnClassA() {
        Class<?> modelClass = CommonServiceUtils.findServiceModelClass(aService);
        assertThat(modelClass, is(sameInstance(A.class)));
    }

    private static class A extends MongoCommonModel {
    }

    private static class AService extends MongoCommonServiceImpl<A> {
        AService(MongoCommonDao<A> dao) {
            super(dao);
        }
    }
}