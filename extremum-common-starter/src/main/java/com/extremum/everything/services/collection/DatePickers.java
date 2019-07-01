package com.extremum.everything.services.collection;

import com.extremum.common.models.Model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author rpuch
 */
class DatePickers {
    static <T extends Model> List<T> sortModels(List<T> fullList, Comparator<T> comparator) {
        List<T> sortedFullList = new ArrayList<>(fullList);
        sortedFullList.sort(comparator);
        return sortedFullList;
    }
}
