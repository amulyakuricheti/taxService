package com.nike.invoiceshipmentwrkr.configuration.oscar;

import com.nike.cdt.auth.clientsdk.NikeJWTClientAuth;
import com.nike.cdt.auth.clientsdk.exceptions.NikeAuthException;
import com.nike.cdt.auth.consts.HTTPJwtHeaders;
import com.nike.invoiceshipmentwrkr.configuration.jwt.JWTAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * In here we will create custom http builder. Use this builder at any time you would want to
 * provide custom build logic for HttpClient used by HTTP4 component.
 *
 * <p>This builder turns off auto retry on the HttpClient, You MAY we also set custom socket and
 * connection time outs. However, that is not recommended, because this instance is singleton it
 * will be reused by any other http connection. So if you want to have one global setting to it here
 * otherwise do in the URI like this: .toD("http4://" + postToUrl + "?httpClient.SocketTimeout=" +
 * socketTimeout + "&httpClient.ConnectTimeout=" + connectionTimeOut);
 *
 * <p>In addition we add custom HTTP interceptor that creates custom http headers and signs the
 * header with JWT key. In order to use JWT signing you need to have nikeAuthBean bean available for
 * autowiring.
 *
 * <p>To use this configuration refer to it when configuring the http4 component like this:
 * .toD("http4://" + postToUrl + "?httpClientConfigurer=#customJWTNoRetryConfigurer")
 *
 * <p>Notice that we use @Configuration on the class to make the route automatic discovered by
 * Spring Boot
 */
@Slf4j
@Configuration
public class HTTPBuilderOverride implements HTTPJwtHeaders {

    @Autowired(required = false)
    private JWTAuthenticator jwtSigner;

    private HttpRequestInterceptor oscarCamelInterceptor;

    @Autowired
    public void setJwtCamelInterceptor(@Qualifier("oscarCamelInterceptor") HttpRequestInterceptor oscarCamelInterceptor) {
        this.oscarCamelInterceptor = oscarCamelInterceptor;
    }

    @Bean
    public HttpRequestInterceptor jwtCamelInterceptor() {
        return new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                log.info("START: sign outgoing request");
                if (jwtSigner != null && jwtSigner.isJwtEnabled()) {
                    try {
                        ensureJWTAuthenticatorConfig();
                        jwtSigner.sign(request);
                    } catch (NikeAuthException e) {
                        log.error("Attempting to generate JWT has failed", e);
                        throw new RuntimeCamelException("Failed to sign the request with JWT");
                    } catch (Exception exp) {
                        log.error("JWT configuration has failed", exp);
                        throw new RuntimeCamelException("Failed to sign the request with JWT");
                    }
                }
                log.info("END: sign outgoing request");
            }
        };
    }

    @Bean
    public HttpClientConfigurer customJWTNoRetryConfigurer() {
        return new HttpClientConfigurer() {

            @Override
            public void configureHttpClient(HttpClientBuilder clientBuilder) {
                /*
                 * if needed to set custom socket/connection time outs you can
                 * do it here HOWEVER, for finer control you can easily do the
                 * same directly on the route configuration buy passing this in
                 * the URI:
                 * httpClient.SocketTimeout=50&httpClient.ConnectTimeout=1000
                 * RequestConfig requestConfig = RequestConfig .custom()
                 * .setSocketTimeout(0) .setConnectTimeout(0) .build();
                 */
                clientBuilder
                        // remove below comment if you wish to use custom
                        // request configuration
                        // .setDefaultRequestConfig(requestConfig)
                        // add interceptor for JWT
                        .addInterceptorFirst(oscarCamelInterceptor)
                        // turn off default retry on the client
                        .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            }
        };
    }

    @Bean
    public HttpClientConfigurer customJWTNoRetryConfigurerForPAC() {
        return new HttpClientConfigurer() {

            @Override
            public void configureHttpClient(HttpClientBuilder clientBuilder) {
                /*
                 * if needed to set custom socket/connection time outs you can
                 * do it here HOWEVER, for finer control you can easily do the
                 * same directly on the route configuration buy passing this in
                 * the URI:
                 * httpClient.SocketTimeout=50&httpClient.ConnectTimeout=1000
                 * RequestConfig requestConfig = RequestConfig .custom()
                 * .setSocketTimeout(0) .setConnectTimeout(0) .build();
                 */
                clientBuilder
                        // remove below comment if you wish to use custom
                        // request configuration
                        // .setDefaultRequestConfig(requestConfig)
                        // add interceptor for JWT
                        .addInterceptorFirst(jwtCamelInterceptor())
                        // turn off default retry on the client
                        .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            }
        };
    }

    public void ensureJWTAuthenticatorConfig() throws Exception {
        try {
            log.info("Ensuring the existence of Nike JWT Authenticator Config for JWT signing.");
            NikeJWTClientAuth.getFullInstanceID(jwtSigner.getConfigId());
            log.info("JWT Authenticator config exists. Proceeding with signing.");
        } catch (NikeAuthException e) {
            log.warn("Nike JWT Authenticator config does not exist at JWT signing time.");
            jwtSigner.configureWithDefaultSettings();
            log.info("Nike JWT Authenticator config was successfully created for JWT signing");
        }
    }
}
