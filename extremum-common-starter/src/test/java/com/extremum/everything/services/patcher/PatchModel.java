package com.extremum.everything.services.patcher;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ModelName("patchModel")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
class PatchModel extends MongoCommonModel {
    private String name;
}
