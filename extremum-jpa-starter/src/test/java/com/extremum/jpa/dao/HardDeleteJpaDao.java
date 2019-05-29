package com.extremum.jpa.dao;

import com.extremum.jpa.dao.impl.SpringDataJpaCommonDao;
import com.extremum.jpa.models.HardDeleteJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteJpaDao extends SpringDataJpaCommonDao<HardDeleteJpaModel> {
    List<HardDeleteJpaModel> findByName(String name);
}
