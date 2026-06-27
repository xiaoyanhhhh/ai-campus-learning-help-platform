package com.campus.aihelp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AiHelpApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiHelpApplication.class, args);
    }
}
