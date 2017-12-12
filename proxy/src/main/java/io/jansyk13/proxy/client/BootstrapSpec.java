package io.jansyk13.proxy.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class BootstrapSpec {
    private Supplier<? extends EventLoopGroup> eventLoopGroup = () -> new EpollEventLoopGroup(
            Runtime.getRuntime().availableProcessors() * 2,
            new DefaultThreadFactory("proxy")
    );
    private Class<? extends Channel> channel = EpollSocketChannel.class;
    private int port = 0;
    private Collection<Tuple2<ChannelOption, ?>> options = new ArrayList<>();
    private boolean traceTransport;

    private int readTimeoutInSeconds = 30;

    public BootstrapSpec eventLoopGroup(Supplier<? extends EventLoopGroup> eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public BootstrapSpec channel(Class<? extends ServerChannel> channel) {
        this.channel = channel;
        return this;
    }


    public BootstrapSpec option(Tuple2<ChannelOption, ?> option) {
        this.options.add(option);
        return this;
    }

    public BootstrapSpec port(int port) {
        this.port = port;
        return this;
    }

    public BootstrapSpec traceTransport(boolean traceTransport) {
        this.traceTransport = traceTransport;
        return this;
    }

    public BootstrapSpec readTimeoutInSeconds(int timemout) {
        this.readTimeoutInSeconds = timemout;
        return this;
    }

    protected EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup.get();
    }

    protected Class<? extends Channel> getChannel() {
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

    public int getReadTimeoutInSeconds() {
        return readTimeoutInSeconds;
    }
}
