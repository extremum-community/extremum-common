package com.extremum.common.descriptor.serde;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class StringToDescriptorConverter implements Converter<String, Descriptor> {
    private final DescriptorFactory descriptorFactory;

    @Override
    public Descriptor convert(String stringValue) {
        return descriptorFactory.fromExternalId(stringValue);
    }
}
