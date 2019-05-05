package com.sapient.purestream.controller;

import com.sapient.purestream.model.Execution;
import com.sapient.purestream.respository.ExecutionRepository;
import com.sapient.purestream.service.TradeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/exec")
public class ExecutionController {
    private final TradeExecutionService tradeExecutionService;
    private final ExecutionRepository executionRepository;

    public ExecutionController(TradeExecutionService tradeExecutionService
            , ExecutionRepository executionRepository) {
        this.tradeExecutionService = tradeExecutionService;
        this.executionRepository = executionRepository;
    }

    @GetMapping("/getallexecutions")
    public List<Execution> getAllExecutions() {
        List<Execution> executions = executionRepository.findAll();
        return executions;
    }

    @PostMapping("/sendexecmessage")
    public ResponseEntity<String> sendExecMessage() {
        System.out.println("rest exec service called");
        tradeExecutionService.tradeExecution();
        System.out.println("rest exec service returning");
        return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
    }
}
