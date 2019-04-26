package com.sapient.purestream.respository;

import com.sapient.purestream.model.Trade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface TradeRepository extends MongoRepository<Trade, Long> {
    @Query("{ 'orderStatus' : ?0 }")
    List<Trade> findByOrderStatus(String orderStatus);
}
