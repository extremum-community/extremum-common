package com.extremum.everything.services.collection;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author rpuch
 */
final class IdComparator implements Comparator<Serializable> {
    private Class<?> elementClass;

    @Override
    public int compare(Serializable o1, Serializable o2) {
        rememberOrCheckElementClass(o1);
        rememberOrCheckElementClass(o2);

        if (o1 instanceof Comparable) {
            @SuppressWarnings("unchecked") int comparisonResult = ((Comparable) o1).compareTo(o2);
            return comparisonResult;
        }

        return o1.toString().compareTo(o2.toString());
    }

    private void rememberOrCheckElementClass(Object obj) {
        if (obj == null) {
            return;
        }
        Class<?> objClass = obj.getClass();
        if (elementClass == null) {
            elementClass = objClass;
        } else {
            if (objClass != elementClass) {
                String message = String.format("This comparator only supports comparing elements of the same " +
                        "type, but it was given '%s' and '%s'", elementClass, objClass);
                throw new IllegalStateException(message);
            }
        }
    }
}
