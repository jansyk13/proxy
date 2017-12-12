package io.jansyk13.proxy.server;

import io.jansyk13.proxy.handlers.UpstreamConnectingHandler;
import io.jansyk13.proxy.client.DownstreamChannelManager;
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

public class ServerBootstrap {

    private final ServerBootstrapSpec serverBootstrapSpec;
    private final DownstreamChannelManager downstreamChannelManager;

    private volatile boolean started;
    private volatile boolean stopped;
    private io.netty.bootstrap.ServerBootstrap bootstrap;
    private EventLoopGroup group;

    public ServerBootstrap(final ServerBootstrapSpec serverBootstrapSpec, final DownstreamChannelManager downstreamChannelManager) {
        this.serverBootstrapSpec = Objects.requireNonNull(serverBootstrapSpec);
        this.downstreamChannelManager = Objects.requireNonNull(downstreamChannelManager);
    }

    public synchronized void start() throws Exception {
        if (started) {
            throw new IllegalStateException("Already started");
        }

        this.group = serverBootstrapSpec.getEventLoopGroup();
        this.bootstrap = new io.netty.bootstrap.ServerBootstrap()
                .group(group)
                .channel(serverBootstrapSpec.getChannel());


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (serverBootstrapSpec.getTraceTransport()) {
                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                }

                pipeline.addLast(new HttpRequestDecoder());
                pipeline.addLast(new HttpResponseEncoder());

                pipeline.addLast(new UpstreamConnectingHandler(downstreamChannelManager));
            }
        });


        for (Tuple2<ChannelOption, ?> tuple : serverBootstrapSpec.getOptions()) {
            this.bootstrap.option(tuple._1, tuple._2);
        }

        this.bootstrap.bind(serverBootstrapSpec.getPort()).sync();

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
