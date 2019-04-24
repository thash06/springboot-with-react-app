package com.sapient.purestream.respository;

import com.sapient.purestream.model.Trade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends MongoRepository<Trade, Long> {


}
