package com.im.imuserservice;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {
        "com.im.common",       // 扫描公共模块的配置、工具、异常处理
        "com.im.imuserservice" // 扫描当前业务模块自身
})
@MapperScan("com.im.imuserservice.mapper")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class ImUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImUserServiceApplication.class, args);
    }

}
