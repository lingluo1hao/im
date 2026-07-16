package com.im.friendservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@MapperScan("com.im.friendservice.mapper")
@SpringBootApplication(scanBasePackages = "com.im")
@EnableFeignClients(basePackages = "com.im")
public class ImFriendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImFriendServiceApplication.class, args);
    }

}
