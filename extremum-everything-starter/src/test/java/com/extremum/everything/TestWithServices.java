package com.extremum.everything;

import com.extremum.test.containers.CoreServices;

/**
 * Extend this class in your test to make sure that services are started
 * once per all test classes.
 *
 * @author rpuch
 */
public abstract class TestWithServices {
    @SuppressWarnings("unused")
    private static final CoreServices services = new CoreServices();
}
