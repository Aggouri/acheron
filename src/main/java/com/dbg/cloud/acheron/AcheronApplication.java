package com.dbg.cloud.acheron;

import com.dbg.cloud.acheron.config.EnableAcheronZuulProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;

@SpringBootApplication(exclude = {CassandraDataAutoConfiguration.class})
@EnableAcheronZuulProxy
public class AcheronApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcheronApplication.class, args);
    }
}
