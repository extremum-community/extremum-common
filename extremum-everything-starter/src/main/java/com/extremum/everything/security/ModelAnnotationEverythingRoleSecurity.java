package com.extremum.everything.security;

import com.extremum.common.models.Model;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class ModelAnnotationEverythingRoleSecurity implements EverythingRoleSecurity {
    private final RoleChecker roleChecker;
    private final ModelClasses modelClasses;

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();

    public ModelAnnotationEverythingRoleSecurity(RoleChecker roleChecker,
            ModelClasses modelClasses) {
        this.roleChecker = roleChecker;
        this.modelClasses = modelClasses;
    }

    @Override
    public void checkGetAllowed(Descriptor id) {
        get.throwIfNoRolesFor(id);
    }

    @Override
    public void checkPatchAllowed(Descriptor id) {
        patch.throwIfNoRolesFor(id);
    }

    @Override
    public void checkRemovalAllowed(Descriptor id) {
        remove.throwIfNoRolesFor(id);
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

            if (!roleChecker.currentUserHasOneOf(roles)) {
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

    private class Remove extends Operation {
        @Override
        Access extractAccess(EverythingSecured annotation) {
            return annotation.remove();
        }

        @Override
        String name() {
            return "remove";
        }
    }
}
