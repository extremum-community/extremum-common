package io.extremum.dynamic.validator.services.impl;

import io.extremum.dynamic.SchemaPointer;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.Schema;
import io.extremum.dynamic.schema.SchemaProvider;
import io.extremum.dynamic.validator.services.DynamicModelValidator;

public interface SchemaProviderBasedDynamicModelValidator<Provider extends SchemaProvider<? extends Schema<?>, ? extends SchemaPointer<?>>>
        extends DynamicModelValidator<JsonBasedDynamicModel> {
    Provider getSchemaProvider();
}
