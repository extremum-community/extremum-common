package io.extremum.dynamic;

public class DynamicModelSupports {
    public static String collectionNameFromModel(String modelName) {
        return toProperCase(modelName.replaceAll("[\\W]", "_"));
    }

    private static String toProperCase(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}