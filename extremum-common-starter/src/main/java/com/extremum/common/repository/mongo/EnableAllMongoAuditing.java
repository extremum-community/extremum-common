package com.extremum.common.repository.mongo;

import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.lang.annotation.*;

/**
 * @author rpuch
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AllMongoAuditingRegistrar.class)
public @interface EnableAllMongoAuditing {
    /**
   	 * Configures the {@link AuditorAware} bean to be used to lookup the current principal.
   	 *
   	 * @return
   	 */
   	String auditorAwareRef() default "";

   	/**
   	 * Configures whether the creation and modification dates are set. Defaults to {@literal true}.
   	 *
   	 * @return
   	 */
   	boolean setDates() default true;

   	/**
   	 * Configures whether the entity shall be marked as modified on creation. Defaults to {@literal true}.
   	 *
   	 * @return
   	 */
   	boolean modifyOnCreate() default true;

   	/**
   	 * Configures a {@link DateTimeProvider} bean name that allows customizing the {@link org.joda.time.DateTime} to be
   	 * used for setting creation and modification dates.
   	 *
   	 * @return
   	 */
   	String dateTimeProviderRef() default "";
}
