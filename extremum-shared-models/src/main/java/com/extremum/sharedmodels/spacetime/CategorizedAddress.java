package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.spacetime.ComplexAddress;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
public class CategorizedAddress {
    private String category;
    private String caption;
    private ComplexAddress address;
}
