package com.dbg.cloud.acheron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class AcheronApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcheronApplication.class, args);
    }
}
