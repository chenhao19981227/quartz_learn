package com.ch.quartz_learn;

import com.ch.quartz_learn.tools.DFUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("HelloJob.execute  "+ DFUtil.format(new Date())+"  "+Thread.currentThread().getName());
    }
}
