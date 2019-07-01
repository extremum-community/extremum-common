package com.extremum.common.service;

import com.extremum.common.exceptions.CommonException;
import com.extremum.common.response.Alert;

import java.util.Collection;
import java.util.Objects;

/**
 * @author rpuch
 */
public final class AddAlert implements Problems {
    private final Collection<Alert> alerts;

    public AddAlert(Collection<Alert> alerts) {
        Objects.requireNonNull(alerts, "Alerts collection cannot be null");

        this.alerts = alerts;
    }

    @Override
    public void accept(CommonException e) {
        alerts.addAll(e.getAlerts());
    }
}
