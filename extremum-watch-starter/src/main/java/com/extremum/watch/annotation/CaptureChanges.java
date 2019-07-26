package com.extremum.watch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on methods in services to indicate that method need a watch logic.
 * Now used only on {@link com.extremum.everything.services.PatcherService}
 *
 * @apiNote IMPORTANT! If you call method from interface, you need to set this annotation on interface instead of implementation class!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CaptureChanges {
}
