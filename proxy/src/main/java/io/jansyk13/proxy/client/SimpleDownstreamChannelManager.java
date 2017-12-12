package io.jansyk13.proxy.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.vavr.Tuple2;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SimpleDownstreamChannelManager implements DownstreamChannelManager {

    private final BootstrapSpec bootstrapSpec;
    private final Bootstrap bootstrap;

    public SimpleDownstreamChannelManager(final BootstrapSpec bootstrapSpec) {
        this.bootstrapSpec = Objects.requireNonNull(bootstrapSpec);

        this.bootstrap = new Bootstrap()
                .group(bootstrapSpec.getEventLoopGroup())
                .channel(bootstrapSpec.getChannel())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (bootstrapSpec.getTraceTransport()) {
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        }
                        pipeline.addLast(new ReadTimeoutHandler(bootstrapSpec.getReadTimeoutInSeconds(), TimeUnit.SECONDS));
                        pipeline.addLast(new HttpRequestEncoder());
                        pipeline.addLast(new HttpResponseDecoder());
                    }
                });
        for (Tuple2<ChannelOption, ?> tuple : bootstrapSpec.getOptions()) {
            bootstrap.option(tuple._1, tuple._2);
        }
    }

    @Override
    public Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
        Bootstrap clone = bootstrap.clone();

        DefaultPromise<Channel> promise = new DefaultPromise<>(bootstrapSpec.getEventLoopGroup().next());
        ChannelFuture channelFuture = clone.connect("localhost", bootstrapSpec.getPort() );

        channelFuture.addListener(f -> {
            if (f.isSuccess()) {
                promise.setSuccess(channelFuture.channel());
            } else {
                promise.setFailure(f.cause());
            }
        });

        return promise;
    }

    @Override
    public ChannelDisposer disposer() {
        return channel -> channel.closeFuture();
    }
}
