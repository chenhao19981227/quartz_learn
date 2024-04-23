package com.ch.quartz_learn.jobConfig;

import com.ch.quartz_learn.job.SpringBeanJob2;
import com.ch.quartz_learn.service.HelloService;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class JobConfig {
    @Bean
    public JobDetail springJobDetail(){
        return JobBuilder.newJob(SpringBeanJob2.class)
                .withIdentity("springJobDetail")
                .storeDurably()
                .build();
    }
    @Bean
    public Trigger springJobTrigger(){
        return TriggerBuilder.newTrigger()
                .forJob("springJobDetail")
                .startNow()
                .build();
    }
}
