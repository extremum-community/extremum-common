package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.serde.mongo.DescriptorDecodeTransformer;
import com.extremum.common.descriptor.serde.mongo.DescriptorEncodeTransformer;
import lombok.RequiredArgsConstructor;
import org.bson.BSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class DescriptorServiceConfigurator {
    private final DescriptorDao descriptorDao;

    public void init() {
        DescriptorService.setDescriptorDao(descriptorDao);
        BSON.addDecodingHook(Descriptor.class, new DescriptorDecodeTransformer());
        BSON.addEncodingHook(Descriptor.class, new DescriptorEncodeTransformer());
    }

}
