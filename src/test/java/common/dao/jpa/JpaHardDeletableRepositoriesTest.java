package common.dao.jpa;

import com.extremum.common.test.TestWithServices;
import models.HardDeletable;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class JpaHardDeletableRepositoriesTest extends TestWithServices {
    @Autowired
    private HardDeletableDao dao;

    @Test
    public void testCreateModel() {
        HardDeletable model = new HardDeletable();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeletable createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    public void testThatFindByIdWorksForAnEntityWithoutDeletedColumn() {
        HardDeletable entity = dao.save(new HardDeletable());
        Optional<HardDeletable> opt = dao.findById(entity.getId());
        assertThat(opt.isPresent(), is(true));
    }

    @Test
    public void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeletable> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeletable> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeletable notDeleted = new HardDeletable();
        notDeleted.setName(uniqueName);

        HardDeletable deleted = new HardDeletable();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }
}
