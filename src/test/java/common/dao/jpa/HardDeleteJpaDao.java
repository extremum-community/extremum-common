package common.dao.jpa;

import com.extremum.common.dao.impl.SpringDataJpaCommonDao;
import models.HardDeleteJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteJpaDao extends SpringDataJpaCommonDao<HardDeleteJpaModel> {
    List<HardDeleteJpaModel> findByName(String name);
}
