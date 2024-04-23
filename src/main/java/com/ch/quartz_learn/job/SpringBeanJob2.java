package com.ch.quartz_learn.job;

import com.ch.quartz_learn.service.HelloService;
import com.ch.quartz_learn.tools.DFUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.StringJoiner;
@Component
public class SpringBeanJob2 extends QuartzJobBean {
    @Autowired
    HelloService helloService;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        StringJoiner outStr = new StringJoiner(" ")
                .add("HelloJob.execute")
                .add(DFUtil.format(new Date()))
                .add(Thread.currentThread().getName())
                .add(context.getTrigger().getKey().getName());
        System.out.println(outStr);
        System.out.println(helloService.helloService());
    }
}
