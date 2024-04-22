package com.ch.quartz_learn._03;

import com.ch.quartz_learn.tools.DFUtil;
import org.quartz.*;

import java.util.Date;
import java.util.StringJoiner;

public class DataJob2 implements Job {
    private String haha;

    public void setHaha(String haha) {
        this.haha = haha;
    }

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
        System.out.println(haha);
    }
}
