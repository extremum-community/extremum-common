package common.dao.mongo;

import com.extremum.common.test.TestWithServices;
import models.HardDeleteMongoModel;
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
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
public class MongoHardDeleteRepositoriesTest extends TestWithServices {
    @Autowired
    private HardDeleteMongoDao dao;

    @Test
    public void testCreateModel() {
        HardDeleteMongoModel model = new HardDeleteMongoModel();
        assertNull(model.getId());
        assertNull(model.getCreated());
        assertNull(model.getModified());

        HardDeleteMongoModel createdModel = dao.save(model);
        assertEquals(model, createdModel);
        assertNotNull(model.getId());
        assertNotNull(model.getCreated());
        assertNotNull(model.getModified());
        assertNotNull(model.getVersion());
        assertFalse(model.getDeleted());
    }

    @Test
    public void testThatFindByIdWorksForAnEntityWithoutDeletedColumn() {
        HardDeleteMongoModel entity = dao.save(new HardDeleteMongoModel());
        Optional<HardDeleteMongoModel> opt = dao.findById(entity.getId());
        assertThat(opt.isPresent(), is(true));
    }

    @Test
    public void testThatSpringDataMagicQueryMethodWorksAndIgnoresDeletedAttribute() {
        String uniqueName = UUID.randomUUID().toString();

        dao.saveAll(oneDeletedAndOneNonDeletedWithGivenName(uniqueName));

        List<HardDeleteMongoModel> results = dao.findByName(uniqueName);
        assertThat(results, hasSize(2));
    }

    @NotNull
    private List<HardDeleteMongoModel> oneDeletedAndOneNonDeletedWithGivenName(String uniqueName) {
        HardDeleteMongoModel notDeleted = new HardDeleteMongoModel();
        notDeleted.setName(uniqueName);

        HardDeleteMongoModel deleted = new HardDeleteMongoModel();
        deleted.setName(uniqueName);
        deleted.setDeleted(true);

        return Arrays.asList(notDeleted, deleted);
    }
}
