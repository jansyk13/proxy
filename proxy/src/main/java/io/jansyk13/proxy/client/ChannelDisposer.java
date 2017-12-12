package io.jansyk13.proxy.client;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;

public interface ChannelDisposer {

    Future<Void> dispose(Channel channel);
}
