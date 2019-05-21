package com.extremum.common.service.impl;

import com.extremum.common.exceptions.CommonException;
import com.extremum.common.response.Alert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
public class AddAlertTest {
    @Test
    public void whenAnExceptionIsConsumed_thenAnAlertShouldBeAddedToTheCollection() {
        List<Alert> alertsList = new ArrayList<>();
        AddAlert alerts = new AddAlert(alertsList);

        alerts.accept(new CommonException("test", 200));

        assertThat(alertsList, hasSize(1));
        Alert alert = alertsList.get(0);
        assertThat(alert.getMessage(), is("test"));
        assertThat(alert.getCode(), is("200"));
    }
}