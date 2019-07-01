package com.extremum.elasticsearch.service;

import com.extremum.common.service.CommonService;
import com.extremum.elasticsearch.dao.SearchOptions;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.List;

/**
 * Common interface for Elasticsearch services.
 */
public interface ElasticsearchCommonService<M extends ElasticsearchCommonModel> extends CommonService<M> {
    List<M> search(String queryString, SearchOptions searchOptions);
}
