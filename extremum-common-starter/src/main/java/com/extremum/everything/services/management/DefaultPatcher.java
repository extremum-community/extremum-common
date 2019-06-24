package com.extremum.everything.services.management;

import com.extremum.common.models.BasicModel;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface DefaultPatcher <M extends BasicModel<?>> extends Patcher<M> {
    M patch(String id, JsonPatch patch);
}
