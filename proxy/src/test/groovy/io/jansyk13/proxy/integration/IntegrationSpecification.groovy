package io.jansyk13.proxy.integration

import io.jansyk13.proxy.downstream.DownstreamBootstrapSpec
import io.jansyk13.proxy.downstream.DownstreamChannelManager
import io.jansyk13.proxy.downstream.SimpleDownstreamChannelManager
import io.jansyk13.proxy.upstream.UpstreamBootstrap
import io.jansyk13.proxy.upstream.UpstreamBootstrapSpec
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
    protected static UpstreamBootstrap proxy

    def setup() {
        resetJadler()
    }

    def setupSpec() {
        client = Dsl.asyncHttpClient(
                Dsl.config().setMaxRequestRetry(0)
                        .setProxyServer(Dsl.proxyServer("localhost", 8080))
                        .setRequestTimeout(60000)
        )

        def spec = new DownstreamBootstrapSpec()
                .eventLoopGroup { new EpollEventLoopGroup(2, new DefaultThreadFactory("downstream")) }
                .port(7070)
                .traceTransport(true)
        def channelManager = downstreamChannelManager(spec)

        def serverSpec = new UpstreamBootstrapSpec()
                .eventLoopGroup { new EpollEventLoopGroup(2, new DefaultThreadFactory("upstream")) }
                .port(8080)
                .traceTransport(true)
        proxy = new UpstreamBootstrap(serverSpec, channelManager)
        proxy.start()


        initJadlerUsing(new JdkStubHttpServer(7070));
    }

    def cleanupSpec() {
        closeJadler();
    }

    DownstreamChannelManager downstreamChannelManager(DownstreamBootstrapSpec downstreamBootstrapSpec) {
        return new SimpleDownstreamChannelManager(downstreamBootstrapSpec)
    }
}
