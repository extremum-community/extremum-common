package com.extremum.common.service.impl;

import com.extremum.common.exceptions.CommonException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author rpuch
 */
public class ThrowOnAlertTest {
    private final Problems problems = new ThrowOnAlert();
    private final CommonException exception = new CommonException("test", 200);

    @Test
    public void whenAnExceptionIsConsumed_thenItShouldBeThrown() {
        try {
            problems.accept(exception);
            fail("An exception should be thrown");
        } catch (CommonException e) {
            assertThat(e, is(sameInstance(exception)));
        }
    }
}