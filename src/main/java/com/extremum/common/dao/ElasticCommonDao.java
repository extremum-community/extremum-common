package com.extremum.common.dao;

import com.extremum.common.models.ElasticCommonModel;

import java.util.List;
import java.util.Map;

public interface ElasticCommonDao<Model extends ElasticCommonModel> extends CommonDao<Model, String> {
    // TODO: move back to CommonDao
    boolean isDeleted(String id);

    List<Model> search(String queryString);

    <N extends Model> N save(N model);

    boolean patch(String id, String painlessQuery);

    boolean patch(String id, String painlessScript, Map<String, Object> params);
}
