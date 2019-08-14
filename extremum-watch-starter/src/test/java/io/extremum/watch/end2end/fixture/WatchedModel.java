package io.extremum.watch.end2end.fixture;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
import io.extremum.security.ExtremumRequiredRoles;
import io.extremum.security.NoDataSecurity;
import io.extremum.watch.annotation.CapturedModel;
import io.extremum.authentication.api.RolesConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rpuch
 */
@ModelName(WatchedModel.MODEL_NAME)
@CapturedModel
@ExtremumRequiredRoles(defaultAccess = RolesConstants.ANONYMOUS)
@NoDataSecurity
@Getter @Setter @ToString
public class WatchedModel extends MongoCommonModel {
    public static final String MODEL_NAME = "E2EWatchedModel";

    private String name;
}