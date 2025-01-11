package com.example.reactivespring;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

public class ReplayProcessorTest {
    @Test
    void replayProcessor() {
        int historySize = 2;
        boolean unbounded = false;
        Sinks.Many<String> processor = Sinks.<String>many()
                .replay()
                .limit(historySize);

        produce(processor);
        consume(processor.asFlux());
    }

    void produce(Sinks.Many<String> processor) {
        processor.emitNext("1", (a, b) -> true);
        processor.emitNext("2", (a, b) -> true);
        processor.emitNext("3", (a, b) -> true);
        processor.emitComplete((a, b) -> true);
    }

    void consume(Flux<String> processor){
        for (int i = 0; i<5; i++){
            StepVerifier
                    .create(processor)
                    .expectNext("2")
                    .expectNext("3")
                    .verifyComplete();
        }
    }
}
