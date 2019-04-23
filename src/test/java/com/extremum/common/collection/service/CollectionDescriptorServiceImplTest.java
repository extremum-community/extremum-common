package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private CollectionDescriptorDao collectionDescriptorDao;

    @Test
    public void test() {
        CollectionDescriptor descriptor = new CollectionDescriptor("123");

        collectionDescriptorService.store(descriptor);

        verify(collectionDescriptorDao).store(descriptor);
    }
}