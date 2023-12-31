package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication(exclude=DataSourceAutoConfiguration.class)
@ServletComponentScan   //扫描WebFilter注解，从而创建过滤器
@EnableTransactionManagement
@EnableCaching  //开启Spring Cache注解
public class ReggieApplication {
    public static void main(String[] args){
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功......");
    }
}

