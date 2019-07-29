package com.extremum.watch.repositories;

import com.extremum.watch.models.TextWatchEvent;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
//TODO add inheritance for WatchEvent and maintain it on repositories
public interface TextWatchEventRepository extends MongoRepository<TextWatchEvent, ObjectId> {
    List<TextWatchEvent> findAllByCreatedAfter(ZonedDateTime start);
}
