package common.dao.mongo;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author rpuch
 */
@Document(EntityWithPolymorphicField.COLLECTION)
@ModelName(EntityWithPolymorphicField.COLLECTION)
class EntityWithPolymorphicField extends MongoCommonModel {
    static final String COLLECTION = "EntityWithPolymorphicField";

    @Getter
    @Setter
    private Object polymorphicField;
}
