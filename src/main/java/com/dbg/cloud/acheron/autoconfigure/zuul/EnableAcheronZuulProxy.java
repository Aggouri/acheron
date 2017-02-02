package com.dbg.cloud.acheron.autoconfigure.zuul;

import com.dbg.cloud.acheron.autoconfigure.zuul.AcheronZuulConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * See {@link org.springframework.cloud.netflix.zuul.EnableZuulProxy}.
 * <p>
 * The difference is that we import a different configuration.
 */
@EnableCircuitBreaker
@EnableDiscoveryClient
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AcheronZuulConfiguration.class)
public @interface EnableAcheronZuulProxy {
}