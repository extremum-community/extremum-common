package com.extremum.elastic.service.impl;

import com.extremum.common.exceptions.CommonException;
import com.extremum.common.response.Alert;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public final class AddAlert implements Problems {
    private final Collection<Alert> alerts;

    @Override
    public void accept(CommonException e) {
        alerts.add(e.getFirstAlert());
    }
}
