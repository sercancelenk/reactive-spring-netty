package com.example.reactivespring;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@Log4j2
public class EmitterProcessorTest {
    public static void main(String[] args) throws InterruptedException {
        Sinks.Many<Integer> processor = Sinks
                .<Integer>many()
                .unicast()
                .onBackpressureBuffer();

        EmitterProcessorTest a = new EmitterProcessorTest();
        Thread.ofVirtual().start(()->{
            a.produce(processor);
        });
        Thread.ofVirtual()
                .start(()->{
                    a.consume(processor);
                });
        Util.sleep(10000);
    }
    @Test
    public void emitterProcessor(){
        Sinks.Many<Integer> processor = Sinks
                .<Integer>many()
                .unicast()
                .onBackpressureBuffer();

        produce(processor);
        consume(processor);

    }

    private void consume(Sinks.Many<Integer> processor) {
        processor
                .asFlux()
                .map(a -> {
                    log.info("Received data {}", a);
                    return a;
                })
                .subscribe();
    }

    private void produce(Sinks.Many<Integer> processor) {
        Sinks.EmitFailureHandler handler = (signalType, emitResult) -> {
            log.info("Signal Type : {}", signalType);
            log.info("EmitResult: {}", emitResult.isSuccess());
            return true;
        };

        processor.emitNext(1, handler);
        Util.sleep(1000);
        processor.emitNext(2, handler);
        Util.sleep(1000);
        processor.emitNext(3, handler);
        Util.sleep(1000);
        processor.emitComplete(handler);
        Util.sleep(1000);
    }



}
