package com.im.nettyserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.im")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.im")
public class ImNettyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImNettyServerApplication.class, args);
    }

}
