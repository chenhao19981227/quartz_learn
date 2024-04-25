package com.ch.quartz_learn.jobConfig;

import com.ch.quartz_learn.job.SpringBeanJob2;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobClusterInit {
    @Autowired
    public Scheduler scheduler;

    @PostConstruct
    public void initJob() throws SchedulerException{
        startSpringJob("job-1","trigger-1");
        startSpringJob("job-2","trigger-2");
        startSpringJob("job-3","trigger-3");
    }

    private void startSpringJob(String jobName, String triggerName) throws SchedulerException {
        JobDetail detail= JobBuilder.newJob(SpringBeanJob2.class)
                .withIdentity(jobName)
                .build();
        Trigger trigger=TriggerBuilder.newTrigger()
                .withIdentity(triggerName)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(5))
                .build();
        scheduler.scheduleJob(detail,trigger);
    }
}
