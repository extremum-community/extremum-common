package com.extremum.everything.support;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import org.springframework.core.ResolvableType;

/**
 * @author rpuch
 */
class CommonServiceUtils {
    static boolean isCommonServiceOfModelClass(CommonService<?, ?> service, Class<? extends Model> modelClass) {
        Class<?> serviceModelClass = findServiceModelClass(service);
        return serviceModelClass == modelClass;
    }

    static Class<?> findServiceModelClass(CommonService<?, ?> service) {
        ResolvableType commonServiceInterface = findCommonServiceInterface(service);
        return findModelGeneric(commonServiceInterface, service);
    }

    private static ResolvableType findCommonServiceInterface(CommonService<?, ?> service) {
        ResolvableType currentType = ResolvableType.forClass(service.getClass());
        ResolvableType commonServiceInterface = ResolvableType.NONE;

        do {
            for (ResolvableType iface : currentType.getInterfaces()) {
                if (iface.getRawClass() == CommonService.class) {
                    commonServiceInterface = iface;
                    break;
                }
            }
            currentType = currentType.getSuperType();
        } while (commonServiceInterface == ResolvableType.NONE && currentType != ResolvableType.NONE);

        return commonServiceInterface;
    }

    private static Class<?> findModelGeneric(ResolvableType commonServiceInterface, CommonService<?, ?> service) {
        for (ResolvableType generic : commonServiceInterface.getGenerics()) {
            Class<?> resolvedGeneric = generic.resolve();
            if (resolvedGeneric != null && Model.class.isAssignableFrom(resolvedGeneric)) {
                return resolvedGeneric;
            }
        }

        throw new IllegalStateException("For class " + service.getClass() + " did not find model generic");
    }
}
