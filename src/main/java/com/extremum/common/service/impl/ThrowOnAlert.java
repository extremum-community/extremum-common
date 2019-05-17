package com.extremum.common.service.impl;

import com.extremum.common.exceptions.CommonException;

/**
 * @author rpuch
 */
public final class ThrowOnAlert implements Problems {
    @Override
    public void accept(CommonException e) {
        throw e;
    }
}
