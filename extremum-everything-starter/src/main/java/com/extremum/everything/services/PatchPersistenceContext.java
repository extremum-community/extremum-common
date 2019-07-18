package com.extremum.everything.services;

import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PatchPersistenceContext<M extends Model> {
    /**
     * Found by ID model. Before patching
     */
    private final M originalModel;
    private final M patchedModel;

    private M currentStateModel;

    public PatchPersistenceContext(M originalModel, M patchedModel) {
        this.originalModel = originalModel;
        this.patchedModel = patchedModel;

        currentStateModel = originalModel;
    }

    public String modelName() {
        return ModelUtils.getModelName(originalModel);
    }
}
