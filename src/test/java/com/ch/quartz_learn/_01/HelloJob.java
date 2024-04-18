package com.ch.quartz_learn._01;

import com.ch.quartz_learn.tools.DFUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.StringJoiner;

public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        StringJoiner outStr = new StringJoiner(" ")
                .add("HelloJob.execute")
                .add(DFUtil.format(new Date()))
                .add(Thread.currentThread().getName())
                .add(jobExecutionContext.getTrigger().getKey().getName());
        System.out.println(outStr);
    }
}
