package com.extremum.elasticsearch.dao;

import com.extremum.common.dao.CommonDao;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.List;
import java.util.Map;

public interface ElasticsearchCommonDao<Model extends ElasticsearchCommonModel> extends CommonDao<Model, String> {

    List<Model> search(String queryString);

    boolean patch(String id, String painlessScript);

    boolean patch(String id, String painlessScript, Map<String, Object> scriptParams);
}
