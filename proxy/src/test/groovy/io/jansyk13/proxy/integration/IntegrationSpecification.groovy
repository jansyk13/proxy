package io.jansyk13.proxy.integration

import io.jansyk13.proxy.client.BootstrapSpec
import io.jansyk13.proxy.client.SimpleDownstreamChannelManager
import io.jansyk13.proxy.server.ServerBootstrap
import io.jansyk13.proxy.server.ServerBootstrapSpec
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import spock.lang.Specification

import static net.jadler.Jadler.closeJadler
import static net.jadler.Jadler.initJadlerUsing
import static net.jadler.Jadler.resetJadler

abstract class IntegrationSpecification extends Specification {

    protected static AsyncHttpClient client
    protected static ServerBootstrap proxy

    def setup() {
        resetJadler()
    }

    def setupSpec() {
        client = Dsl.asyncHttpClient(
                Dsl.config().setMaxRequestRetry(0)
                        .setProxyServer(Dsl.proxyServer("localhost", 8080))
                        .setRequestTimeout(60000)
        )

        def spec = new BootstrapSpec()
                .eventLoopGroup { new EpollEventLoopGroup(2, new DefaultThreadFactory("downstream")) }
                .port(7070)
                .traceTransport(true)
        def channelManager = new SimpleDownstreamChannelManager(spec)

        def serverSpec = new ServerBootstrapSpec()
                .eventLoopGroup { new EpollEventLoopGroup(2, new DefaultThreadFactory("upstream")) }
                .port(8080)
                .traceTransport(true)
        proxy = new ServerBootstrap(serverSpec, channelManager)
        proxy.start()


        initJadlerUsing(new JdkStubHttpServer(7070));
    }

    def cleanupSpec() {
        closeJadler();
    }
}
