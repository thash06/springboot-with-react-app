package com.sapient.purestream.respository;

import com.sapient.purestream.model.Execution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionRepository extends MongoRepository<Execution, Long> {

}
