package com.extremum.elasticsearch.service;

import com.extremum.common.service.CommonService;
import com.extremum.elasticsearch.model.ElasticCommonModel;

/**
 * Common interface for Elasticsearch services.
 */
public interface ElasticCommonService<M extends ElasticCommonModel> extends CommonService<String, M> {
}
