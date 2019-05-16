package common.dao.jpa;

import com.extremum.common.dao.impl.SpringDataJpaCommonDao;
import models.HardDeletable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeletableDao extends SpringDataJpaCommonDao<HardDeletable> {
    List<HardDeletable> findByName(String name);
}
