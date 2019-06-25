package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.everything.services.management.Patcher;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface DefaultPatcher <M extends Model> extends Patcher<M> {
    M patch(String id, JsonPatch patch);
}
