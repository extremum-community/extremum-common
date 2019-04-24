package com.extremum.common.dao;

import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;

public interface MongoCommonDao<M extends MongoCommonModel> extends CommonDao<M, ObjectId> {
}
