package io.jansyk13.proxy.integration

import net.jadler.Jadler
import org.asynchttpclient.RequestBuilder
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@Unroll
class SimpleHttpProxySpec extends IntegrationSpecification {

    def 'proxy HTTP requests method=#method status=#status'() {
        given:
        Jadler.onRequest()
                .havingMethodEqualTo(method)
                .respond()
                .withStatus(status)

        when:
        def future = client.executeRequest(new RequestBuilder(method))

        then:
        future.get().statusCode == status

        where:
        method   | requestBody | status | responseBody
        "GET"    | 200
        "GET"    | 300
        "GET"    | 400
        "GET"    | 500
        "POST"   | 200
        "POST"   | 300
        "POST"   | 400
        "POST"   | 500
        "PUT"    | 200
        "PUT"    | 300
        "PUT"    | 400
        "PUT"    | 500
        "DELETE" | 200
        "DELETE" | 300
        "DELETE" | 400
        "DELETE" | 500
    }
}
