package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.common.utils.ModelUtils;
import io.extremum.security.services.DataAccessChecker;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

/**
 * @author rpuch
 */
public final class AccessCheckersDataSecurity implements DataSecurity {
    private final List<DataAccessChecker<?>> checkers;
    private final RoleChecker roleChecker;
    private final PrincipalSource principalSource;

    private final Operation get = new Get();
    private final Operation patch = new Patch();
    private final Operation remove = new Remove();
    private final Operation watch = new Watch();

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

    private Optional<DataAccessChecker<Model>> findChecker(Model model) {
        String modelName = modelName(model);
        @SuppressWarnings("unchecked")
        DataAccessChecker<Model> castChecker = (DataAccessChecker<Model>) ModelServices.findServiceForModel(
                modelName, checkers);
        return Optional.ofNullable(castChecker);
    }

    private String modelName(Model model) {
        return ModelUtils.getModelName(model);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void validateModelClassConfig(Model model, Optional<DataAccessChecker<Model>> checker) {
        boolean annotatedWithNoDataSecurity = isAnnotatedWithNoDataSecurity(model);
        throwIfNoCheckerAndNoAnnotation(model, checker, annotatedWithNoDataSecurity);
        throwIfBothCheckerAndAnnotation(model, checker, annotatedWithNoDataSecurity);
    }

    private boolean isAnnotatedWithNoDataSecurity(Model model) {
        return DataSecurityAnnotations.annotatedWithNoDataSecurity(model.getClass());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void throwIfNoCheckerAndNoAnnotation(Model model, Optional<DataAccessChecker<Model>> checker,
            boolean annotatedWithNoDataSecurity) {
        if (!checker.isPresent() && !annotatedWithNoDataSecurity) {
            String message = String.format(
                    "No DataAccessChecker was found and no @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new ExtremumSecurityException(message);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void throwIfBothCheckerAndAnnotation(Model model, Optional<DataAccessChecker<Model>> checker,
            boolean annotatedWithNoDataSecurity) {
        if (checker.isPresent() && annotatedWithNoDataSecurity) {
            String message = String.format(
                    "Both DataAccessChecker was found and @NoDataSecurity annotation exists on '%s'",
                    modelName(model));
            throw new ExtremumSecurityException(message);
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

    @Override
    public void checkWatchAllowed(Model model) {
        watch.checkDataAccess(model);
    }

    private class SimpleCheckerContext implements CheckerContext {
        @Override
        public Optional<String> getCurrentPrincipal() {
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

            Optional<DataAccessChecker<Model>> optChecker = findChecker(model);
            validateModelClassConfig(model, optChecker);

            optChecker.ifPresent(checker -> {
                SimpleCheckerContext context = new SimpleCheckerContext();
                if (!allowed(model, checker, context)) {
                    throw new ExtremumAccessDeniedException("Access denied");
                }
            });
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

    private class Watch extends Operation {
        @Override
        boolean allowed(Model model, DataAccessChecker<Model> checker, CheckerContext context) {
            return checker.allowedToWatch(model, context);
        }
    }
}
