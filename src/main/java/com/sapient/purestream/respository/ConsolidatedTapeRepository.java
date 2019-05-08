package com.sapient.purestream.respository;

import com.sapient.purestream.model.ConsolidatedTape;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsolidatedTapeRepository extends MongoRepository<ConsolidatedTape, Long> {

}
