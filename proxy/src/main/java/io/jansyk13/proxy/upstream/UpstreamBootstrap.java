package io.jansyk13.proxy.upstream;

import io.jansyk13.proxy.handlers.UpstreamConnectingHandler;
import io.jansyk13.proxy.downstream.DownstreamChannelManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.vavr.Tuple2;

import java.util.Objects;

public class UpstreamBootstrap {

    private final UpstreamBootstrapSpec upstreamBootstrapSpec;
    private final DownstreamChannelManager downstreamChannelManager;

    private volatile boolean started;
    private volatile boolean stopped;
    private io.netty.bootstrap.ServerBootstrap bootstrap;
    private EventLoopGroup group;

    public UpstreamBootstrap(final UpstreamBootstrapSpec upstreamBootstrapSpec, final DownstreamChannelManager downstreamChannelManager) {
        this.upstreamBootstrapSpec = Objects.requireNonNull(upstreamBootstrapSpec);
        this.downstreamChannelManager = Objects.requireNonNull(downstreamChannelManager);
    }

    public synchronized void start() throws Exception {
        if (started) {
            throw new IllegalStateException("Already started");
        }

        this.group = upstreamBootstrapSpec.getEventLoopGroup();
        this.bootstrap = new io.netty.bootstrap.ServerBootstrap()
                .group(group)
                .channel(upstreamBootstrapSpec.getChannel());


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (upstreamBootstrapSpec.getTraceTransport()) {
                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                }

                pipeline.addLast(new HttpRequestDecoder());
                pipeline.addLast(new HttpResponseEncoder());

                pipeline.addLast(new UpstreamConnectingHandler(downstreamChannelManager));
            }
        });


        for (Tuple2<ChannelOption, ?> tuple : upstreamBootstrapSpec.getOptions()) {
            this.bootstrap.option(tuple._1, tuple._2);
        }

        this.bootstrap.bind(upstreamBootstrapSpec.getPort()).sync();

        started = true;
    }

    public synchronized void stop() throws Exception {
        if (!started) {
            throw new IllegalStateException("Not running");
        }

        if (stopped) {
            throw new IllegalStateException("Already stopped");
        }

        this.group.shutdownGracefully().sync();

        stopped = true;
    }
}
