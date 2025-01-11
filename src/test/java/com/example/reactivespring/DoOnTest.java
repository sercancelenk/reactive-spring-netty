package com.example.reactivespring;


import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class DoOnTest {

    @Test
    public void doOnTest() {
        var signals = new ArrayList<Signal<Integer>>();
        var nextValues = new ArrayList<Integer>();
        var subscriptions = new ArrayList<Subscription>();
        var exceptions = new ArrayList<Throwable>();
        var finallySignals = new ArrayList<SignalType>();

        Flux<Integer> on = Flux
                .<Integer>create(sink -> {
                    sink.next(1);
                    Util.sleep(1000);
                    sink.next(2);
                    Util.sleep(1000);
                    sink.next(3);
                    sink.error(new IllegalArgumentException("oops!"));
                    sink.complete();
                })
                .doOnNext(nextValues::add)
                .doOnEach(signals::add)
                .doOnSubscribe(subscriptions::add)
                .doOnError(IllegalArgumentException.class, exceptions::add)
                .doFinally(finallySignals::add);

        StepVerifier
                .create(on)
                .expectNext(1, 2, 3)
                .expectError(IllegalArgumentException.class)
                .verify();

        signals.forEach(log::info);
        assertThat(signals.size()).isEqualTo(4);

        finallySignals.forEach(log::info);
        assertThat(finallySignals.size()).isEqualTo(1);

        subscriptions.forEach(log::info);
        assertThat(subscriptions.size()).isEqualTo(1);

        exceptions.forEach(log::info);
        assertThat(exceptions.size()).isEqualTo(1);

        exceptions.forEach(log::info);
        assertThat(exceptions.size()).isEqualTo(1);
        assertThat(exceptions.get(0)).isInstanceOf(IllegalArgumentException.class);

        nextValues.forEach(log::info);
        assertThat(nextValues).containsAll(Arrays.asList(1, 2, 3));




    }
}
