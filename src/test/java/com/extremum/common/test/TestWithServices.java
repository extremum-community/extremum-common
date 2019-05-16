package com.extremum.common.test;

import com.extremum.common.containers.Services;

/**
 * Extend this class in your test to make sure that services are started
 * once per all test classes.
 *
 * @author rpuch
 */
public abstract class TestWithServices {
    @SuppressWarnings("unused")
    private static final Services services = new Services();
}
