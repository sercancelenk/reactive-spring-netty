package com.example.reactivespring;

import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.Flow;

public class FlowAndReactiveStreamsTest {
    @Test
    public void convert(){
        Flux<Integer> original = Flux.range(0, 10);

        Flow.Publisher<Integer> rangeOfIntegersAsJdkFlow = FlowAdapters
                .toFlowPublisher(original);
        Publisher<Integer> rangeOfIntegersAsreactiveStream = FlowAdapters
                .toPublisher(rangeOfIntegersAsJdkFlow);

        StepVerifier.create(original).expectNextCount(10).verifyComplete();

        StepVerifier.create(rangeOfIntegersAsreactiveStream)
                .expectNextCount(10)
                .verifyComplete();

        Flux<Integer> rangeOfIntegersAsReactorFluxAgain = JdkFlowAdapter
                .flowPublisherToFlux(rangeOfIntegersAsJdkFlow);
        StepVerifier.create(rangeOfIntegersAsReactorFluxAgain)
                .expectNextCount(10)
                .verifyComplete();
    }
}
