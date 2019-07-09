package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.security.Access;
import com.extremum.everything.security.AccessChecker;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingSecured;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
// TODO: invent a better name
public class EverythingSecurityImpl implements EverythingSecurity {
    private final AccessChecker accessChecker;
    private final ModelClasses modelClasses;

    public EverythingSecurityImpl(AccessChecker accessChecker,
            ModelClasses modelClasses) {
        this.accessChecker = accessChecker;
        this.modelClasses = modelClasses;
    }

    @Override
    public void checkRolesAllowCurrentUserToGet(Descriptor id) {
        Class<Model> modelClass = modelClasses.getClassByModelName(id.getModelType());

        EverythingSecured everythingSecured = modelClass.getAnnotation(EverythingSecured.class);
        Access getAccess = everythingSecured.get();
        String[] roles = getAccess.value();

        if (!accessChecker.currentUserHasOneOf(roles)) {
            throw new EverythingAccessDeniedException("Access denied");
        }
    }
}
