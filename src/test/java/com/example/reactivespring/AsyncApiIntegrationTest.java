package com.example.reactivespring;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Log4j2
public class AsyncApiIntegrationTest {
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        AsyncApiIntegrationTest a = new AsyncApiIntegrationTest();

        Flux<Integer> integers = Flux.<Integer>create(emitter -> a.launch(emitter, 5))
                .log()
                .sort(Comparator.<Integer>reverseOrder());

        integers.subscribe();
        a.sleep(6000);
    }
    @Test
    void async() {
        Flux<Integer> integers = Flux.create(emitter -> this.launch(emitter, 5));

        StepVerifier
                .create(integers.doFinally(signalType -> this.executorService.shutdown()))
                .expectNextCount(5)
                .verifyComplete();
    }

    private void launch(FluxSink<Integer> emitter, int i) {
        this.executorService.submit(()->{
            var integer = new AtomicInteger();
            assertNotNull(emitter);
            while(integer.get() < i){
                double random = 1.0;
                emitter.next(integer.incrementAndGet());
                this.sleep((long)random*1_000);
            }
            emitter.complete();
        });
    }

    private void sleep(long l) {
        try{
            Thread.sleep(l);
        }catch (Exception e){
            log.error(e);
        }
    }
}
