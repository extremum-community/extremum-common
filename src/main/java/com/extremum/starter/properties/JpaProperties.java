package com.extremum.starter.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("jpa")
public class JpaProperties {

    private String uri;
    private String username;
    private String password;
    private boolean generateDdl;
    private boolean showSql;
    private List<String> packageNames;

}