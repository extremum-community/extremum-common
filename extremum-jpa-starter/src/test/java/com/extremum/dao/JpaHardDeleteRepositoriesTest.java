package com.extremum.dao;

import com.extremum.TestWithServices;
import com.extremum.models.HardDeleteJpaModel;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;


@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class JpaHardDeleteRepositoriesTest extends TestWithServices {
    @Autowired
    private HardDeleteJpaDao dao;

    @Test
    public void testCreateModel() {
        HardDeleteJpaModel model = new HardDeleteJpaModel();
        Assertions.assertNull(model.getId());
        Assertions.assertNull(model.getCreated());
        Assertions.assertNull(model.getModified());

        HardDeleteJpaModel createdModel = dao.save(model);
        Assertions.assertEquals(model, createdModel);
        Assertions.assertNotNull(model.getId());
        Assertions.assertNotNull(model.getCreated());
        Assertions.assertNotNull(model.getModified());
        Assertions.assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    public void testThatFindByIdWorksForAnEntityWithoutDeletedColumn() {
        HardDeleteJpaModel entity = dao.save(new HardDeleteJpaModel());
        Optional<HardDeleteJpaModel> opt = dao.findById(entity.getId());
        MatcherAssert.assertThat(opt.isPresent(), Matchers.is(true));
    }

    @Test
    public void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeleteJpaModel> results = dao.findByName(uniqueName);
        MatcherAssert.assertThat(results, Matchers.hasSize(2));
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
