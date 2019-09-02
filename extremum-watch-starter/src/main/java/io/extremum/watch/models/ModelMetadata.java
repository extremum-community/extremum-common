package io.extremum.watch.models;

import io.extremum.common.model.BasicModel;
import io.extremum.common.model.Model;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.utils.ModelUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModelMetadata {
    private String id;
    private String model;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private Long version;

    public static ModelMetadata fromModel(Model model) {
        if (model instanceof PersistableCommonModel) {
            return new ModelMetadata((PersistableCommonModel) model);
        }
        if (model instanceof BasicModel) {
            return new ModelMetadata((BasicModel) model);
        }
        throw new IllegalStateException(model.getClass() + " is not a BasicModel");
    }

    public ModelMetadata(PersistableCommonModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model),
                model.getCreated(), model.getModified(), model.getVersion());
    }

    public ModelMetadata(BasicModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model), null, null, null);
    }
}
