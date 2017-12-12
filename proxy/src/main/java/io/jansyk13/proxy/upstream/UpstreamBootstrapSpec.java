package io.jansyk13.proxy.upstream;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class UpstreamBootstrapSpec {

    private Supplier<? extends EventLoopGroup> eventLoopGroup = () -> new EpollEventLoopGroup(
            Runtime.getRuntime().availableProcessors() * 2,
            new DefaultThreadFactory("proxy")
    );
    private Class<? extends ServerChannel> channel = EpollServerSocketChannel.class;
    private int port = 0;
    private Collection<Tuple2<ChannelOption, ?>> options = new ArrayList<>();
    private boolean traceTransport;

    public UpstreamBootstrapSpec eventLoopGroup(Supplier<EventLoopGroup> eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public UpstreamBootstrapSpec channel(Class<? extends ServerChannel> channel) {
        this.channel = channel;
        return this;
    }


    public UpstreamBootstrapSpec option(Tuple2<ChannelOption, ?> option) {
        this.options.add(option);
        return this;
    }

    public UpstreamBootstrapSpec port(int port) {
        this.port = port;
        return this;
    }

    public UpstreamBootstrapSpec traceTransport(boolean traceTransport) {
        this.traceTransport = traceTransport;
        return this;
    }

    protected EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup.get();
    }

    protected Class<? extends ServerChannel> getChannel() {
        return channel;
    }

    protected Collection<Tuple2<ChannelOption, ?>> getOptions() {
        return options;
    }

    protected int getPort() {
        return port;
    }

    protected boolean getTraceTransport() {
        return traceTransport;
    }
}

