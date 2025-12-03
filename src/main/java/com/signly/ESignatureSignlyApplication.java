package com.signly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
public class ESignatureSignlyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ESignatureSignlyApplication.class, args);
    }

}