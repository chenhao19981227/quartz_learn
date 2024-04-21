package com.ch.quartz_learn._04;

import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.beans.Transient;
import java.util.concurrent.TimeUnit;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
@SpringBootTest
public class QuartzTest_SpringBean {
    @Test
    public void test(){
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            JobDetail job = newJob(SpringBeanJob.class)
                    .withIdentity("job1", "group1")
                    .usingJobData("haha","I am JobDetail")
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .usingJobData("haha","I am Trigger")
                    .startNow()
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule("0/1 * * * * ? *")
                    )
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
            TimeUnit.SECONDS.sleep(1);
            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}