package com.im.groupservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@MapperScan("com.im.groupservice.mapper")
@SpringBootApplication(scanBasePackages = "com.im")
@EnableFeignClients(basePackages = "com.im")
public class ImGroupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImGroupServiceApplication.class, args);
    }

}
