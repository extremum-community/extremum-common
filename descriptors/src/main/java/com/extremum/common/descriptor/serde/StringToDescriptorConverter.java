package com.extremum.common.descriptor.serde;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import org.springframework.core.convert.converter.Converter;

public class StringToDescriptorConverter implements Converter<String, Descriptor> {
    @Override
    public Descriptor convert(String s) {
        return DescriptorFactory.fromExternalId(s);
    }
}
