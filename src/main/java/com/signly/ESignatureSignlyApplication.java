package com.signly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
@EnableScheduling
@EnableAsync
public class ESignatureSignlyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ESignatureSignlyApplication.class, args);
    }

}