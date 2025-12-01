package com.deally;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
public class ESignatureSignlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ESignatureSignlyApplication.class, args);
    }

}