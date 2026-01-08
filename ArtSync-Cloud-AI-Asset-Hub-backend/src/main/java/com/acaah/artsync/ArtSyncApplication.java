package com.acaah.artsync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.acaah.artsync.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class ArtSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtSyncApplication.class, args);
    }

}
