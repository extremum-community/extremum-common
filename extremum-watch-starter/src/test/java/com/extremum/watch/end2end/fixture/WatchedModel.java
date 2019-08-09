package com.extremum.watch.end2end.fixture;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.security.ExtremumRequiredRoles;
import com.extremum.security.NoDataSecurity;
import com.extremum.sharedmodels.annotation.CapturedModel;
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
