package common.dao.jpa;

import com.extremum.common.test.TestWithServices;
import models.HardDeleteJpaModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class JpaHardDeleteRepositoriesTest extends TestWithServices {
    @Autowired
    private HardDeleteJpaDao dao;

    @Test
    public void testCreateModel() {
        HardDeleteJpaModel model = new HardDeleteJpaModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteJpaModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    public void testThatFindByIdWorksForAnEntityWithoutDeletedColumn() {
        HardDeleteJpaModel entity = dao.save(new HardDeleteJpaModel());
        Optional<HardDeleteJpaModel> opt = dao.findById(entity.getId());
        assertThat(opt.isPresent(), is(true));
    }

    @Test
    public void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeleteJpaModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeleteJpaModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeleteJpaModel notDeleted = new HardDeleteJpaModel();
        notDeleted.setName(uniqueName);

        HardDeleteJpaModel deleted = new HardDeleteJpaModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }
}
