package com.extremum.jpa.dao;

import com.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import com.extremum.jpa.models.TestBasicJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestBasicJpaModelDao extends SpringDataJpaCommonDao<TestBasicJpaModel> {
    List<TestBasicJpaModel> findByName(String name);
}
