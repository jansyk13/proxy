package io.jansyk13.proxy.integration

import net.jadler.Jadler
import net.jadler.stubbing.RequestStubbing
import org.asynchttpclient.RequestBuilder
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@Unroll
class SimpleHttpProxySpec extends IntegrationSpecification {

    def 'proxy HTTP requests method=#method requestBody=#requestBody status=#status responseBody=#responseBody'() {
        given:
        def requestStubbing = Jadler.onRequest()
                .havingMethodEqualTo(method)

        if (requestBody) {
            requestStubbing.havingBodyEqualTo(requestBody)
        }
        def responseStubbing = requestStubbing
                .respond()
                .withStatus(status)
        if (responseBody) {
            responseStubbing.withBody(responseBody)
        }

        def requestBuilder = new RequestBuilder(method)
        if (requestBody) {
            requestBuilder.setBody(requestBody)
        }

        when:
        def response = client.executeRequest(requestBuilder).get()

        then:
        response.statusCode == status
        !responseBody ^ response.responseBody == responseBody

        where:
        method | requestBody | status | responseBody
        'GET'  | null        | 200    | null
        'GET'  | null        | 200    | 'test'
        'GET'  | null        | 300    | null
        'GET'  | null        | 300    | 'test'
        'GET'  | null        | 400    | null
        'GET'  | null        | 400    | 'test'
        'GET'  | null        | 500    | null
        'GET'  | null        | 500    | 'test'
        'POST' | null        | 200    | null
//        'POST' | 'test'      | 200    | null
//        'POST' | null        | 200    | 'test'
//        'POST' | 'test'      | 200    | 'test'
//        'POST'   | 300
//        'POST'   | 400
//        'POST'   | 500
//        'PUT'    | 200
//        'PUT'    | 300
//        'PUT'    | 400
//        'PUT'    | 500
//        'DELETE' | 200
//        'DELETE' | 300
//        'DELETE' | 400
//        'DELETE' | 500
    }
}
