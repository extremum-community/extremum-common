package io.extremum.everything.services.patcher;

import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.models.annotation.ModelName;
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