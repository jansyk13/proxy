package io.jansyk13.proxy.downstream;

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

public class DownstreamBootstrapSpec {
    private Supplier<? extends EventLoopGroup> eventLoopGroup = () -> new EpollEventLoopGroup(
            Runtime.getRuntime().availableProcessors() * 2,
            new DefaultThreadFactory("proxy")
    );
    private Class<? extends Channel> channel = EpollSocketChannel.class;
    private String address = "localhost";
    private int port = 0;
    private Collection<Tuple2<ChannelOption, ?>> options = new ArrayList<>();
    private boolean traceTransport;

    private int readTimeoutInSeconds = 30;

    public DownstreamBootstrapSpec eventLoopGroup(Supplier<? extends EventLoopGroup> eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public DownstreamBootstrapSpec channel(Class<? extends ServerChannel> channel) {
        this.channel = channel;
        return this;
    }


    public DownstreamBootstrapSpec option(Tuple2<ChannelOption, ?> option) {
        this.options.add(option);
        return this;
    }

    public DownstreamBootstrapSpec port(int port) {
        this.port = port;
        return this;
    }

    public DownstreamBootstrapSpec address(String address) {
        this.address = address;
        return this;
    }

    public DownstreamBootstrapSpec traceTransport(boolean traceTransport) {
        this.traceTransport = traceTransport;
        return this;
    }

    public DownstreamBootstrapSpec readTimeoutInSeconds(int timemout) {
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

    public String getAddress() {
        return address;
    }

    public boolean getTraceTransport() {
        return traceTransport;
    }

    public int getReadTimeoutInSeconds() {
        return readTimeoutInSeconds;
    }
}
