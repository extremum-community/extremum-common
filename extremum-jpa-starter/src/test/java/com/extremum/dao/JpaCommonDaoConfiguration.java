package com.extremum.dao;

import com.extremum.config.JpaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import(JpaConfiguration.class)
public class JpaCommonDaoConfiguration {
}
