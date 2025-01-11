## Reactive Spring Boot with Netty

### Netty Settings

```
For Netty Server, one of the essential class is NetUtil.java
For high incoming requests, you should increase http event queue by the below

somaxconn | SO_BACKLOG
# Linux
sudo sysctl -w net.core.somaxconn=65535
# or permanent in /etc/sysctl.conf:
net.core.somaxconn=65535

# macOS
sudo sysctl -w kern.ipc.somaxconn=65535

ChannelOption.SO_BACKLOG, 2048
```

### Netty Configurations
```
@Configuration
@Slf4j
public class NettyConfig {

    @Value("${netty.worker-count:8}")
    private int workerCount;

    private EventLoopGroup eventLoopGroup;

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
            ThreadFactory factory = Thread.ofVirtual()
                    .name("v-server-", 0)
                    .factory();
            eventLoopGroup = new NioEventLoopGroup(workerCount, factory);

            return httpServer
                    .runOn(eventLoopGroup)
                    .option(ChannelOption.SO_BACKLOG, 2048)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // TCP optimizations
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // Increase buffer sizes for better throughput
                    .childOption(ChannelOption.SO_RCVBUF, 4 * 1024)
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

    @PreDestroy
    public void destroyEventLoopGroup() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
            log.info("Event loop group is closed successfully.");
        }
    }
} 
```
### Resources
- https://dzone.com/articles/thousands-of-socket-connections-in-java-practical
