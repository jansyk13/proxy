package io.jansyk13.proxy.downstream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.Promise;

public class PooledDownstreamChannelManager extends DownstreamChannelManager{

    protected PooledDownstreamChannelManager(DownstreamBootstrapSpec downstreamBootstrapSpec) {
        super(downstreamBootstrapSpec);
    }

    @Override
    public Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
        return null;
    }

    @Override
    public DownstreamChannelDisposer disposer() {
        return null;
    }
}
