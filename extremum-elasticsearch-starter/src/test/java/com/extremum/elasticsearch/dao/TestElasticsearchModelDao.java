package com.extremum.elasticsearch.dao;

import com.extremum.common.repository.SeesSoftlyDeletedRecords;
import com.extremum.elasticsearch.dao.impl.SpringDataElasticsearchCommonDao;
import com.extremum.elasticsearch.model.TestElasticsearchModel;

import java.util.List;

public interface TestElasticsearchModelDao extends SpringDataElasticsearchCommonDao<TestElasticsearchModel> {
    List<TestElasticsearchModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestElasticsearchModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
