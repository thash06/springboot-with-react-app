package com.sapient.purestream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapient.purestream.constants.OrderStatus;
import com.sapient.purestream.constants.Side;
import com.sapient.purestream.model.Trade;
import com.sapient.purestream.respository.TradeRepository;
import com.sapient.purestream.service.SequeneGeneratorService;
import com.sapient.purestream.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SpringBootAndReactApp implements CommandLineRunner {

    @Autowired
    private TradeRepository tradeRepository;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAndReactApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
     /*   ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("sampletrade.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        System.out.println("content---------------------" + content);
        ObjectMapper mapper = new ObjectMapper();
        List<Trade> trades = mapper.readValue(content, new TypeReference<List<Trade>>() {
        });
       for(Trade trade : trades) {
           this.tradeRepository.save(trade);
       }*/

       /* Trade t1 = new Trade();
        t1.setId(1L);
        t1.setQuantity(1);
        t1.setSide(Side.BUY);
        t1.setTicker("MSFT");
        t1.setOrderType("5-15%");
        t1.setOrderStatus(OrderStatus.NEW);

        Trade t2 = new Trade();
        t2.setId(2L);
        t2.setQuantity(2);
        t2.setSide(Side.SELL);
        t2.setTicker("GOOG");
        t2.setOrderType("5-15%");
        t2.setOrderStatus(OrderStatus.NEW);

        this.tradeRepository.save(t1);
        this.tradeRepository.save(t2);*/
    }
}
