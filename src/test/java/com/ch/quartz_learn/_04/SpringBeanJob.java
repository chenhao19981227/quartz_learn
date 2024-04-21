package com.ch.quartz_learn._04;

import com.ch.quartz_learn.service.HelloService;
import com.ch.quartz_learn.tools.DFUtil;
import com.ch.quartz_learn.tools.SpringContextUtil;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.StringJoiner;
public class SpringBeanJob implements Job {
    HelloService helloService;
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
        helloService= (HelloService) SpringContextUtil.getApplicationContext()
                .getBean(StringUtils.uncapitalize(HelloService.class.getSimpleName()));
        System.out.println(helloService);
    }
}
