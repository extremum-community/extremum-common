package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface Patcher<M extends Model> {
    M patch(String id, JsonPatch patch);
}
