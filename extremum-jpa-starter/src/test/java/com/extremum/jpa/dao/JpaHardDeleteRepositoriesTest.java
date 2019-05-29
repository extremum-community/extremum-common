package com.extremum.jpa.dao;

import com.extremum.jpa.TestWithServices;
import com.extremum.jpa.models.HardDeleteJpaModel;
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;


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
