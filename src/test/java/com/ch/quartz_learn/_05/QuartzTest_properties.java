package com.ch.quartz_learn._05;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzTest_properties {
    public static void main(String[] args) {
        try {
            Scheduler scheduler=StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            System.out.println(scheduler.getSchedulerName());
            System.out.println(scheduler.getMetaData().getThreadPoolSize());
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}