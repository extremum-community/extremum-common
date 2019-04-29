package com.extremum.common.dao;

import com.extremum.common.models.ElasticCommonModel;
import com.extremum.common.models.ElasticData;

import java.util.List;
import java.util.Map;

public interface ElasticCommonDao<M extends ElasticCommonModel> extends CommonDao<M, String> {
    List<ElasticData> search(String queryString);

    ElasticData persist(ElasticData model, Long seqNo, Long primaryTerm);

    boolean patch(String id, String painlessQuery);

    boolean patch(String id, String painlessScript, Map<String, Object> params);
}
