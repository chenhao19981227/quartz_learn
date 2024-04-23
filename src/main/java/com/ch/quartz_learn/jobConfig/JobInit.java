package com.ch.quartz_learn.jobConfig;

import com.ch.quartz_learn.job.SpringBeanJob2;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobInit {
    @Autowired
    Scheduler scheduler;
    @PostConstruct
    public void initJob() throws SchedulerException {
        JobDetail jobDetail= JobBuilder.newJob(SpringBeanJob2.class)
                .build();
        Trigger trigger= TriggerBuilder.newTrigger()
                .startNow()
                .build();
        scheduler.scheduleJob(jobDetail,trigger);
    }
}
