package io.extremum.common.reactive;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;

@Getter
@Setter
@ConfigurationProperties(prefix = "extremum.reactive")
@Valid
public class ReactiveProperties {
    private int reactifierThreadPoolSize = 4;
}
