package com.ch.quartz_learn;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
public class QuartzLearnApplication {
   @Value("${spring.properties.org.quartz.scheduler.instanceId}")
    private String instanceId;
    @PostConstruct
    public void printInstanceId(){
        System.out.println(instanceId);
    }
    public static void main(String[] args) {
        SpringApplication.run(QuartzLearnApplication.class, args);
    }

}
