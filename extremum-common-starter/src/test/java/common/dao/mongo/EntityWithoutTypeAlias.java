package common.dao.mongo;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import org.springframework.data.mongodb.core.mapping.Document;

import static common.dao.mongo.EntityWithoutTypeAlias.COLLECTION;

/**
 * @author rpuch
 */
@Document(COLLECTION)
@ModelName(COLLECTION)
class EntityWithoutTypeAlias extends MongoCommonModel {
    static final String COLLECTION = "EntityWithoutTypeAlias";
}
