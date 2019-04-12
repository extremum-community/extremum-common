package com.extremum.common.stucts;

import lombok.Getter;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class IdListOrObjectListStruct<ID extends Serializable, T> {
    public Type type;
    @Valid
    public List<ID> idList;
    @Valid
    public List<T> objectList;

    public IdListOrObjectListStruct(List<T> objectList) {
        this.objectList = objectList;
    }

    public IdListOrObjectListStruct(Type type, List<ID> idList, List<T> objectList) {
        this.type = type;
        this.idList = idList;
        this.objectList = objectList;
    }

    public static <ID extends Serializable, T> IdListOrObjectListStruct<ID, T> createContainsIdList(List<ID> ids) {
        return new IdListOrObjectListStruct<>(Type.idList, ids, null);
    }

    public static <ID extends Serializable, T> IdListOrObjectListStruct<ID, T> createContainsObjectList(List<T> objects) {
        return new IdListOrObjectListStruct<>(Type.objectList, null, objects);
    }

    public IdListOrObjectListStruct() {
        this(Type.unknown, new ArrayList<>(), new ArrayList<>());
    }

    public boolean isContainsIdList() {
        return type == Type.idList;
    }

    public boolean isContainsObjectList() {
        return type == Type.objectList;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public void changeTypeToIdList(List<ID> idList) {
        type = Type.idList;
        this.idList = idList;
        objectList = null;
    }

    public <InternalIdType> Set<InternalIdType> getInternalIdSet(Function<ID, InternalIdType> externalToInternalIdConverter) {
        if (isContainsObjectList() || !isKnown()) {
            return Collections.emptySet();
        }

        return idList.stream().map(externalToInternalIdConverter).collect(Collectors.toSet());
    }

    public void changeTypeToObjectList(List<T> objectList) {
        type = Type.idList;
        this.objectList = objectList;
        idList = null;
    }

    public Set<String> getIdSetOrNull(Function<? super ID, String> idListConverter) {
        if (isContainsIdList()) {
            return idList.stream().map(idListConverter).collect(Collectors.toSet());
        }

        return null;
    }

    public enum Type {
        unknown,
        idList,
        objectList
    }
}
