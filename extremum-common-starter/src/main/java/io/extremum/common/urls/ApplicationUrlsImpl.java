package io.extremum.common.urls;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author rpuch
 */
public class ApplicationUrlsImpl implements ApplicationUrls {
    @Override
    public String createExternalUrl(String applicationUri) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(applicationUri)
                .build()
                .toUriString();
    }
}
