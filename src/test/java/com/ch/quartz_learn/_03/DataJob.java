package com.ch.quartz_learn._03;

import com.ch.quartz_learn.tools.DFUtil;
import org.quartz.*;

import java.util.Date;
import java.util.StringJoiner;

public class DataJob implements Job {
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDetail jobDetail = jobContext.getJobDetail();
        Trigger trigger = jobContext.getTrigger();
        StringJoiner outStr = new StringJoiner(" ")
                .add("HelloJob.execute")
                .add(DFUtil.format(new Date()))
                .add(Thread.currentThread().getName())
                .add(jobContext.getTrigger().getKey().getName());
        System.out.println(outStr);
        System.out.println(jobDetail.getJobDataMap().get("haha"));
        System.out.println(trigger.getJobDataMap().get("haha"));
        System.out.println(jobContext.getMergedJobDataMap().get("haha"));
    }
}
