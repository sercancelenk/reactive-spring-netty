package com.example.reactivespring.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.LoopResources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@Slf4j
public class NettyConfig {

    @Value("${netty.worker-count:8}")
    private int workerCount;

    private EventLoopGroup parentEventLoopGroup;

    @Bean
    public Scheduler ioScheduler() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("v-io-", 0)
                .factory();
        ExecutorService executorService = Executors.newThreadPerTaskExecutor(factory);
        return Schedulers.fromExecutorService(executorService);
    }

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        try {
            NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

            factory.addServerCustomizers(customizer());
            return factory;
        } finally {
            log.info("Event loop group is initialized successfully.");
        }
    }

    private NettyServerCustomizer customizer() {
        return httpServer -> {
            parentEventLoopGroup = createOptimalEventLoopGroup();

            return httpServer
                    .runOn(parentEventLoopGroup)
                    .option(ChannelOption.SO_BACKLOG, 4096)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_REUSEADDR, true)

                    // TCP optimizations
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // Increase buffer sizes for better throughput
                    .childOption(ChannelOption.SO_RCVBUF, 4 * 1024).noSSL()
                    .childOption(ChannelOption.SO_SNDBUF, 4 * 1024)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(4 * 1024, 8 * 1024));
        };
    }

    @Bean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        factory.setLoopResources(LoopResources.create("reactor-http", 1,
                workerCount > 0 ? workerCount : Runtime.getRuntime().availableProcessors(),
                true));
        return factory;
    }

    private EventLoopGroup createOptimalEventLoopGroup() {
        int threads = workerCount > 0 ? workerCount : Runtime.getRuntime().availableProcessors();
        ThreadFactory factory = Thread.ofVirtual().name("v-netty-", 0).factory();

        try {
            Class.forName("io.netty.channel.epoll.Epoll");
            if (Epoll.isAvailable()) {
                log.info("Using Epoll for Linux environment");
                return new EpollEventLoopGroup(threads, factory);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Epoll not available");
        }

        log.info("Using NIO as fallback");
        return new NioEventLoopGroup(threads, factory);
    }

    @PreDestroy
    public void destroyEventLoopGroup() {
        if (parentEventLoopGroup != null) {
            parentEventLoopGroup.shutdownGracefully();
            log.info("Event loop group is closed successfully.");
        }
    }
} 