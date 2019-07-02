package com.sapient.purestream.reactive.repository;

import com.sapient.purestream.reactive.model.Trade;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface TradeRepository extends ReactiveCrudRepository<Trade, Long> {
    @Query("{ 'orderStatus' : ?0 }")
    Flux<Trade> findByOrderStatus(String orderStatus);

    @Query("{'$and' : [{'orderType' : ?0}, {'side' : {$ne: ?1}}, {'ticker':?2}] }")
    Flux<Trade> findByOrdertypeSideAndTicker(String orderType, String side, String ticker);

    @Query("{'$and' : [{'orderType' : ?0}, {'ticker' : ?1}] }")
    Flux<Trade> findByOrderTypeAndTicker(String orderType, String ticker);

    @Query("{'ticker':?0}")
    Flux<Trade> findByTicker(String ticker);

    @Query("{'quantity':?0}")
    Flux<Trade> findByQuantity(int quantity);
}
