package com.sapient.purestream.reactive.repository;

import com.sapient.purestream.reactive.model.Execution;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionRepository extends ReactiveCrudRepository<Execution, Long> {

}
