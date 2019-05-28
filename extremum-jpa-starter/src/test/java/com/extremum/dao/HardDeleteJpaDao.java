package com.extremum.dao;

import com.extremum.dao.impl.SpringDataJpaCommonDao;
import com.extremum.models.HardDeleteJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteJpaDao extends SpringDataJpaCommonDao<HardDeleteJpaModel> {
    List<HardDeleteJpaModel> findByName(String name);
}
