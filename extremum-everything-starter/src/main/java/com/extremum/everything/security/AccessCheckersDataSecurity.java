package com.extremum.everything.security;

import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.security.services.DataAccessChecker;
import com.extremum.everything.services.management.EverythingServices;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author rpuch
 */
public final class AccessCheckersDataSecurity implements EverythingDataSecurity {
    private final List<DataAccessChecker<?>> checkers;
    private final RoleChecker roleChecker;
    private final PrincipalSource principalSource;

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();

    public AccessCheckersDataSecurity(List<DataAccessChecker<?>> checkers,
            RoleChecker roleChecker, PrincipalSource principalSource) {
        this.checkers = ImmutableList.copyOf(checkers);
        this.roleChecker = roleChecker;
        this.principalSource = principalSource;
    }

    @Override
    public void checkGetAllowed(Model model) {
        get.checkDataAccess(model);
    }

    private DataAccessChecker<Model> findChecker(Model model) {
        String modelName = modelName(model);
        @SuppressWarnings("unchecked")
        DataAccessChecker<Model> castChecker = (DataAccessChecker<Model>) EverythingServices.findServiceForModel(
                modelName, checkers);
        return castChecker;
    }

    private String modelName(Model model) {
        return ModelUtils.getModelName(model);
    }

    private void validateModelClassConfig(Model model, DataAccessChecker<Model> checker) {
        boolean annotatedWithNoDataSecurity = isAnnotatedWithNoDataSecurity(model);
        throwIfNoCheckerAndNoAnnotation(model, checker, annotatedWithNoDataSecurity);
        throwIfBothCheckerAndAnnotation(model, checker, annotatedWithNoDataSecurity);
    }

    private boolean isAnnotatedWithNoDataSecurity(Model model) {
        return DataSecurityAnnotations.annotatedWithNoDataSecurity(model.getClass());
    }

    private void throwIfNoCheckerAndNoAnnotation(Model model, DataAccessChecker<Model> checker,
            boolean annotatedWithNoDataSecurity) {
        if (checker == null && !annotatedWithNoDataSecurity) {
            String message = String.format(
                    "No DataAccessChecker was found and no @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new EverythingEverythingException(message);
        }
    }

    private void throwIfBothCheckerAndAnnotation(Model model, DataAccessChecker<Model> checker,
            boolean annotatedWithNoDataSecurity) {
        if (checker != null && annotatedWithNoDataSecurity) {
            String message = String.format(
                    "Both DataAccessChecker was found and @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new EverythingEverythingException(message);
        }
    }

    @Override
    public void checkPatchAllowed(Model model) {
        patch.checkDataAccess(model);
    }

    @Override
    public void checkRemovalAllowed(Model model) {
        remove.checkDataAccess(model);
    }

    private class SimpleCheckerContext implements CheckerContext {
        @Override
        public String getCurrentPrincipal() {
            return principalSource.getPrincipal();
        }

        @Override
        public boolean currentUserHasOneOf(String... roles) {
            return roleChecker.currentUserHasOneRoleOf(roles);
        }
    }

    private abstract class Operation {
        void checkDataAccess(Model model) {
            if (model == null) {
                return;
            }

            DataAccessChecker<Model> checker = findChecker(model);
            validateModelClassConfig(model, checker);

            if (checker != null) {
                SimpleCheckerContext context = new SimpleCheckerContext();
                if (!allowed(model, checker, context)) {
                    throw new EverythingAccessDeniedException("Access denied");
                }
            }
        }

        abstract boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context);
    }

    private class Get extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToGet(model, context);
        }
    }

    private class Patch extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToPatch(model, context);
        }
    }

    private class Remove extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToRemove(model, context);
        }
    }
}
