package com.sapient.purestream.reactive.service;

import com.sapient.purestream.reactive.model.DatabaseSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author tarhashm
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoSequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private MongoSequenceGeneratorService testClass = new MongoSequenceGeneratorService();

    @Test
    public void generateSequence() {
        DatabaseSequence counter = new DatabaseSequence();
        counter.setId("someId");
        counter.setSeq(123l);
        Mockito.when(mongoOperations.findAndModify(Mockito.any(Query.class), Mockito.any(Update.class),
                Mockito.any(FindAndModifyOptions.class), Mockito.any())).thenReturn(counter);

        long returnValue = testClass.generateSequence("someSequence");
        //assert

        assertNotNull(returnValue);
        assertEquals(123l, returnValue);

    }
}