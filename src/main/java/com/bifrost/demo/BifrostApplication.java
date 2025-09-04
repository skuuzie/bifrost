package com.bifrost.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class BifrostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BifrostApplication.class, args);
    }

}
