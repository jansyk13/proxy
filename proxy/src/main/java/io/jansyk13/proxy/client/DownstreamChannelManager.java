package io.jansyk13.proxy.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.Promise;

public interface DownstreamChannelManager {

    Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext);

    ChannelDisposer disposer();
}
