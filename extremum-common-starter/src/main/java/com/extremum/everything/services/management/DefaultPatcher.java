package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface DefaultPatcher <M extends Model> extends Patcher<M> {
    M patch(String id, JsonPatch patch);
}
