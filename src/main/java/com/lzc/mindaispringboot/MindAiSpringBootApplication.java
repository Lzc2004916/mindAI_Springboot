package com.lzc.mindaispringboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lzc.mindaispringboot.mappper")
public class MindAiSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindAiSpringBootApplication.class, args);
    }

}
