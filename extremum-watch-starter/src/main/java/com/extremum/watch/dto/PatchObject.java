package com.extremum.watch.dto;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.ModelUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class PatchObject {
    @JsonProperty
    private final String id;
    @JsonProperty
    private final String model;
    @JsonProperty
    private final ZonedDateTime created;
    @JsonProperty
    private final ZonedDateTime modified;
    @JsonProperty
    private final Long version;

    public static PatchObject fromModel(Model model) {
        if (model instanceof PersistableCommonModel) {
            return new PatchObject((PersistableCommonModel) model);
        }
        if (model instanceof BasicModel) {
            return new PatchObject((BasicModel) model);
        }
        throw new IllegalStateException(model.getClass() + " is not a BasicModel");
    }

    public PatchObject(PersistableCommonModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model),
                model.getCreated(), model.getModified(), model.getVersion());
    }

    public PatchObject(BasicModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model), null, null, null);
    }
}
