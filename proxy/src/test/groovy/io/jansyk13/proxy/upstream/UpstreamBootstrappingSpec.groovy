package io.jansyk13.proxy.upstream

import io.jansyk13.proxy.downstream.DownstreamBootstrapSpec
import io.jansyk13.proxy.downstream.DownstreamChannelDisposer
import io.jansyk13.proxy.downstream.DownstreamChannelManager
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpRequest
import io.netty.util.concurrent.Promise
import io.vavr.Tuple2
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class UpstreamBootstrappingSpec extends Specification {

    def 'bootstrap works'() {
        given:
        def bootstrap = new UpstreamBootstrap(spec, new DownstreamChannelManager(new DownstreamBootstrapSpec()) {
            @Override
            Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
                return null
            }

            @Override
            DownstreamChannelDisposer disposer() {
                return null
            }
        })

        when:
        bootstrap.start()

        then:
        bootstrap.stop()

        where:
        spec << [
                new UpstreamBootstrapSpec(),
                new UpstreamBootstrapSpec()
                        .port(8088),
                new UpstreamBootstrapSpec()
                        .eventLoopGroup { new NioEventLoopGroup() }
                        .channel(NioServerSocketChannel),
                new UpstreamBootstrapSpec()
                        .traceTransport(true),
                new UpstreamBootstrapSpec()
                        .option(Tuple2.of(ChannelOption.SO_BACKLOG, 100))
        ]
    }
}
