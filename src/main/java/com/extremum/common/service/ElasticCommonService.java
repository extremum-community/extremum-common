package com.extremum.common.service;

import com.extremum.common.models.ElasticCommonModel;

/**
 * Common interface for Elasticsearch services.
 */
public interface ElasticCommonService<M extends ElasticCommonModel> extends CommonService<String, M> {
}
