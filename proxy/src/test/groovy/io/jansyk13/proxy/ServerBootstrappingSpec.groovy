package io.jansyk13.proxy

import io.jansyk13.proxy.client.ChannelDisposer
import io.jansyk13.proxy.client.DownstreamChannelManager
import io.jansyk13.proxy.server.ServerBootstrap
import io.jansyk13.proxy.server.ServerBootstrapSpec
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
class ServerBootstrappingSpec extends Specification {

    def 'bootstrap works'() {
        given:
        def bootstrap = new ServerBootstrap(spec, new DownstreamChannelManager() {
            @Override
            Promise<Channel> provide(HttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
                return null
            }

            @Override
            ChannelDisposer disposer() {
                return null
            }
        })

        when:
        bootstrap.start()

        then:
        bootstrap.stop()

        where:
        spec << [
                new ServerBootstrapSpec(),
                new ServerBootstrapSpec()
                        .port(8080),
                new ServerBootstrapSpec()
                        .eventLoopGroup { new NioEventLoopGroup() }
                        .channel(NioServerSocketChannel),
                new ServerBootstrapSpec()
                        .traceTransport(true),
                new ServerBootstrapSpec()
                        .option(Tuple2.of(ChannelOption.SO_BACKLOG, 100))
        ]
    }
}
