package com.extremum.everything.security;

import com.extremum.common.utils.AnnotationUtils;

/**
 * @author rpuch
 */
class DataSecurityAnnotations {
    static boolean annotatedWithNoDataSecurity(Class<?> aClass) {
        return AnnotationUtils.findAnnotationDirectlyOrUnderProxy(NoDataSecurity.class, aClass) != null;
    }

    private DataSecurityAnnotations() {
    }
}
