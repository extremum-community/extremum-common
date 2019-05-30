package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

/**
 * @author rpuch
 */
abstract class BaseElasticRepository<M extends ElasticsearchCommonModel> extends SimpleElasticsearchRepository
        implements ElasticsearchCommonDao {
}
