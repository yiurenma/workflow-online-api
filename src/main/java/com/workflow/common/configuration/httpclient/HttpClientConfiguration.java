package com.workflow.common.configuration.httpclient;

import com.workflow.common.configuration.logging.InfoHttpLogWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.zalando.logbook.*;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.core.SplunkHttpLogFormatter;
import org.zalando.logbook.httpclient5.LogbookHttpExecHandler;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Configuration
public class HttpClientConfiguration {

    @Autowired
    Logbook logbook;

    @Bean
    public CloseableHttpClient httpClient5(){
        return HttpClientBuilder.create()
                    .addExecInterceptorFirst(
                            "Logbook",
                            new LogbookHttpExecHandler(logbook)
                    )
                    .disableCookieManagement()
                    .useSystemProperties()
                    .setRetryStrategy(retryHandler())
                    .build();
    }

    @Bean
    public HttpRequestRetryStrategy retryHandler(){
        return new HttpRequestRetryStrategy() {
            @Override
            public boolean retryRequest(HttpRequest httpRequest, IOException e, int i, HttpContext httpContext) {
                log.warn("retry request: " + i + " because " + e);

                if (i >= 5) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (HttpMethod.GET.toString().equalsIgnoreCase(httpRequest.getMethod())){
                    log.info("retry GET request");
                    return true;
                }
                if (e instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (e instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (e instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                if (e instanceof SocketException) {
                    // SocketException : connection reset
                    log.info("retry request with SocketException");
                    return true;
                }
                return false;
            }

            @Override
            public boolean retryRequest(HttpResponse httpResponse, int i, HttpContext httpContext) {
                return false;
            }

            @Override
            public TimeValue getRetryInterval(HttpResponse httpResponse, int i, HttpContext httpContext) {
                return null;
            }
        };
    }
}
