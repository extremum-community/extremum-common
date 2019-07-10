package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.security.Access;
import com.extremum.everything.security.RoleBasedSecurity;
import com.extremum.everything.security.EverythingAccessDeniedException;
import com.extremum.everything.security.EverythingSecured;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
// TODO: invent a better name
public class EverythingSecurityImpl implements EverythingSecurity {
    private final RoleBasedSecurity roleBasedSecurity;
    private final ModelClasses modelClasses;

    public EverythingSecurityImpl(RoleBasedSecurity roleBasedSecurity,
            ModelClasses modelClasses) {
        this.roleBasedSecurity = roleBasedSecurity;
        this.modelClasses = modelClasses;
    }

    @Override
    public void checkRolesAllowCurrentUserToGet(Descriptor id) {
        Class<Model> modelClass = modelClasses.getClassByModelName(id.getModelType());

        EverythingSecured everythingSecured = modelClass.getAnnotation(EverythingSecured.class);
        if (everythingSecured == null) {
            throw new EverythingEverythingException(
                    String.format("Security is not configured for '%s'", id.getModelType()));
        }
        Access getAccess = everythingSecured.get();
        String[] roles = getAccess.value();

        if (roles.length == 0) {
            throw new EverythingEverythingException(
                    String.format("Security is not configured for '%s' for get operation", id.getModelType()));
        }

        if (!roleBasedSecurity.currentUserHasOneOf(roles)) {
            throw new EverythingAccessDeniedException("Access denied");
        }
    }
}
