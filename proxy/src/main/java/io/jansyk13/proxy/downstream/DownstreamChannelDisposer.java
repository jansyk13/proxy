package io.jansyk13.proxy.downstream;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;

public interface DownstreamChannelDisposer {

    Future<Void> dispose(Channel channel);
}
