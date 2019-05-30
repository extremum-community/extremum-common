package com.extremum.elastic.service;

import com.extremum.elastic.model.ElasticCommonModel;

/**
 * Common interface for Elasticsearch services.
 */
public interface ElasticCommonService<M extends ElasticCommonModel> extends CommonService<String, M> {
}
