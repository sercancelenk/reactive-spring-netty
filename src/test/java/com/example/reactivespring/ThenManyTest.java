package com.example.reactivespring;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class ThenManyTest {
    @Test
    void thenMany() {
        var letters = new AtomicInteger();
        var numbers = new AtomicInteger();
        Flux<String> lettersPublisher = Flux.just("a", "b")
                .doOnNext(v -> letters.incrementAndGet());

        Flux<Integer> numbersPublisher = Flux.just(1, 2)
                .doOnNext(v -> numbers.incrementAndGet());

        Flux<Integer> thisBeforeThat =
                lettersPublisher
                        .thenMany(numbersPublisher);
        thisBeforeThat
                .map(a -> {
                    log.info("Receive {}", a);
                    return a;
                })
                .subscribe();

        numbersPublisher
                .zipWith(lettersPublisher)
                .map(a -> {
                    Integer t1 = a.getT1();
                    String t2 = a.getT2();
                    return t1 + t2;
                })
                .log()
                .subscribe();

        Util.sleep(10000);

    }
}
