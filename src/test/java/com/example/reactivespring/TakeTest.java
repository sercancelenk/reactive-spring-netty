package com.example.reactivespring;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class TakeTest {
    public static void main(String[] args) {
        var count = 10;
        Flux<Integer> take = Flux.range(0, 200)
                .delayElements(Duration.ofSeconds(1)).take(count);
        Flux<Integer> takeUntil = Flux.range(0, 120)
                .log()
                .takeUntil(i -> i == 200);
//        take.log().subscribe();
        takeUntil.subscribe();
        Util.sleep(10000);
    }
}
