package com.lzc.mindaispringboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.lzc.mindaispringboot.mappper")
@EnableScheduling //开启任务调度
public class MindAiSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindAiSpringBootApplication.class, args);
    }

}
