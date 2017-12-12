package io.jansyk13.proxy.downstream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
import io.netty.util.concurrent.Promise;
import io.vavr.Tuple2;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for providing and disposing of downstream channels, provides common interface. Channel manager
 * is responsible for creating fully functional downstream channel (codecs, handlers, etc.).
 * Only {@link io.jansyk13.proxy.handlers.DownstreamConnectingHandler} will be added to the end the channel pipeline.
 */
public abstract class DownstreamChannelManager {

    protected final DownstreamBootstrapSpec downstreamBootstrapSpec;

    protected DownstreamChannelManager(DownstreamBootstrapSpec downstreamBootstrapSpec) {
        this.downstreamBootstrapSpec = Objects.requireNonNull(downstreamBootstrapSpec);
    }

    public abstract Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext);

    public abstract DownstreamChannelDisposer disposer();

    protected Bootstrap buildBootstrap() {
        Bootstrap bootstrap = new Bootstrap()
                .group(downstreamBootstrapSpec.getEventLoopGroup())
                .channel(downstreamBootstrapSpec.getChannel())
                .handler(buildChannelInitializer());
        for (Tuple2<ChannelOption, ?> tuple : downstreamBootstrapSpec.getOptions()) {
            bootstrap.option(tuple._1, tuple._2);
        }
        return bootstrap;
    }

    protected ChannelInitializer<Channel> buildChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                if (downstreamBootstrapSpec.getTraceTransport()) {
                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                }
                pipeline.addLast(new ReadTimeoutHandler(downstreamBootstrapSpec.getReadTimeoutInSeconds(), TimeUnit.SECONDS));
                pipeline.addLast(new HttpRequestEncoder());
                pipeline.addLast(new HttpResponseDecoder());
            }
        };
    }

    public DownstreamBootstrapSpec getDownstreamBootstrapSpec() {
        return downstreamBootstrapSpec;
    }
}
