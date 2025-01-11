package com.example.reactivespring;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping
@Log4j2
@RequiredArgsConstructor
public class IndexController {
    private final Scheduler ioScheduler;

    public record State(int i, Date d) {
    }

    private Sinks.Many<Integer> sink;

    @PostConstruct
    public void init() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();

        Flux.interval(Duration.ofSeconds(1))
                .map(Long::intValue)
                .doOnNext(sink::tryEmitNext)
                .subscribe();
    }

    @GetMapping(value = "/cold-numbers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<State> getNumbers() {
        return Flux.range(1, 5)
                .subscribeOn(ioScheduler)
                .map(i -> new State(i, new Date()))
                .doOnSubscribe(subscription -> System.out.println("New subscription to Cold Stream"));
    }

    @GetMapping(value = "/hot-numbers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<State> getHotNumbers() {
        return sink.asFlux()
                .doOnEach(log::info)
                .map(i -> new State(i, new Date()))
                .doOnSubscribe(subscription -> System.out.println("New subscription to Hot Stream"));
    }
}
