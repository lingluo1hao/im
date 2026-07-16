package com.im.pushservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.im")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.im")
public class ImPushServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImPushServiceApplication.class, args);
    }

}
