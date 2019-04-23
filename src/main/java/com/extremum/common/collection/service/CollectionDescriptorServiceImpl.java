package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class CollectionDescriptorServiceImpl implements CollectionDescriptorService {
    private final CollectionDescriptorDao collectionDescriptorDao;

    public CollectionDescriptorServiceImpl(CollectionDescriptorDao collectionDescriptorDao) {
        this.collectionDescriptorDao = collectionDescriptorDao;
    }

    @Override
    public void store(CollectionDescriptor descriptor) {
        collectionDescriptorDao.store(descriptor);
    }
}
