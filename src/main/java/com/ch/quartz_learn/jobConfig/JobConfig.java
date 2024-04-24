package com.ch.quartz_learn.jobConfig;

import com.ch.quartz_learn.job.SpringBeanJob2;
import com.ch.quartz_learn.service.HelloService;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class JobConfig {
    @Bean
    @QuartzDataSource
    public DataSource qDatasource(){
        DriverManagerDataSource dataSource= new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setUrl("jdbc:mysql://localhost:3306/quartz_learn?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        return dataSource;
    }
//    @Bean
//    public JobDetail springJobDetail(){
//        return JobBuilder.newJob(SpringBeanJob2.class)
//                .withIdentity("springJobDetail")
//                .storeDurably()
//                .build();
//    }
//    @Bean
//    public Trigger springJobTrigger(){
//        return TriggerBuilder.newTrigger()
//                .forJob("springJobDetail")
//                .startNow()
//                .build();
//    }
}
