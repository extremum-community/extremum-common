package com.extremum.dao;

import com.extremum.dao.impl.SpringDataJpaCommonDao;
import com.extremum.models.TestBasicJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestBasicJpaModelDao extends SpringDataJpaCommonDao<TestBasicJpaModel> {
    List<TestBasicJpaModel> findByName(String name);
}
