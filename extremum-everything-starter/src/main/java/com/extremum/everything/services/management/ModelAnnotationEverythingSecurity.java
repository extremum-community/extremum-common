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
public class ModelAnnotationEverythingSecurity implements EverythingSecurity {
    private final RoleBasedSecurity roleBasedSecurity;
    private final ModelClasses modelClasses;

    private final Operation get = new Get();
    private final Operation patch = new Patch();

    public ModelAnnotationEverythingSecurity(RoleBasedSecurity roleBasedSecurity,
            ModelClasses modelClasses) {
        this.roleBasedSecurity = roleBasedSecurity;
        this.modelClasses = modelClasses;
    }

    @Override
    public void checkRolesAllowCurrentUserToGet(Descriptor id) {
        get.throwIfNoRolesFor(id);
    }

    @Override
    public void checkRolesAllowCurrentUserToPatch(Descriptor id) {
        patch.throwIfNoRolesFor(id);
    }

    private abstract class Operation {

        void throwIfNoRolesFor(Descriptor id) {
            Class<Model> modelClass = modelClasses.getClassByModelName(id.getModelType());

            EverythingSecured everythingSecured = modelClass.getAnnotation(EverythingSecured.class);
            if (everythingSecured == null) {
                throw new EverythingEverythingException(
                        String.format("Security is not configured for '%s'", id.getModelType()));
            }
            Access getAccess = extractAccess(everythingSecured);
            String[] roles = getAccess.value();

            if (roles.length == 0) {
                String message = String.format("Security is not configured for '%s' for %s operation",
                        id.getModelType(), name());
                throw new EverythingEverythingException(message);
            }

            if (!roleBasedSecurity.currentUserHasOneOf(roles)) {
                throw new EverythingAccessDeniedException("Access denied");
            }
        }

        abstract Access extractAccess(EverythingSecured annotation);

        abstract String name();
    }

    private class Get extends Operation {
        @Override
        Access extractAccess(EverythingSecured annotation) {
            return annotation.get();
        }

        @Override
        String name() {
            return "get";
        }
    }

    private class Patch extends Operation {
        @Override
        Access extractAccess(EverythingSecured annotation) {
            return annotation.patch();
        }

        @Override
        String name() {
            return "patch";
        }
    }
}
