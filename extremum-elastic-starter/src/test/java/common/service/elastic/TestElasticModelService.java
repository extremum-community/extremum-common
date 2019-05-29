package common.service.elastic;

import com.extremum.common.dao.ElasticCommonDao;
import com.extremum.common.service.impl.ElasticCommonServiceImpl;
import models.TestElasticModel;

/**
 * @author rpuch
 */
public class TestElasticModelService extends ElasticCommonServiceImpl<TestElasticModel> {
    public TestElasticModelService(ElasticCommonDao<TestElasticModel> dao) {
        super(dao);
    }
}
