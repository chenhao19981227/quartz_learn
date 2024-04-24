# Quartz

# 一、调度器、触发器以及任务详情

调度器调用触发器，触发器调用任务详情，任务详情对应一个具体任务。

其中，调度器和触发器是一对多的关系，触发器和任务详情是多对一的关系，任务详情和任务也是多对一的关系。

![](.\quartz图片\调度器触发器和任务.jpg)

当创建好`Scheduler`后，需要调用`scheduler.start()`来启动调度器，并在定时任务结束后调用`scheduler.shutdown()`。

对于每个任务，我们需要自己编写一个类，该类实现`Job`接口，并重写其`execute()`方法。

同时，我们需要调用`withIdentity`给任务一个`name`以及`group`，`name`是为了标示任务，`group`是为了方便管理。当我们需要对某些任务做相同的调度时，最好用`group`管理起来。触发器也是一样，需要调用`withIdentity`指定`name`以及`group`。若没有指定`group`，`quartz`会统一将`group`声明为`DEFAULT`。若没有指定`name`，则使用`md5`生成唯一序列。

调度器调用某个触发器触发任务时，需要通过`scheduler.scheduleJob(job, trigger)`实现。

~~~java
package com.ch.quartz_learn;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import java.util.concurrent.TimeUnit;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
public class QuartzTest {

    public static void main(String[] args) {

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            JobDetail job = newJob(HelloJob.class)
                    .withIdentity("job1", "group1")
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(5)
                            .repeatForever())
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
            TimeUnit.SECONDS.sleep(20);
            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("HelloJob.execute  "+ DFUtil.format(new Date())+"  "+Thread.currentThread().getName());
    }
}
~~~

多个触发器的情况：

一般来说Quartz不允许给同一个`JobDetail`分配多个触发器。第一次调用`scheduler.scheduleJob(job, trigger)`会将任务详情和第一个触发器存储在调度器中，第二次调用时会尝试再次存储同一个作业，但是由于两者是同一个任务详情，只是使用了不同的触发器，因此会报错。

要解决这个问题，在创建`trigger`时声明对于的`job`，并手动将`job`缓存进`scheduler`中。

但是要在没有绑定触发器的情况下将`JobDetails`信息持久化时，有一个属性`storeDurably`，如果设置为`true`则无论与其关联的`trigger`是否存在其都会一直存在，否则只要相关联的`trigger`删除掉了其会自动删除掉。所以这里要先设置为`true`。

~~~java
public class QuartzTest_MultiTrigger {

    public static void main(String[] args) {

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            JobDetail job = newJob(HelloJob.class)
                    .withIdentity("job1", "group1")
                    .storeDurably() // 允许在没有绑定触发器的情况下将`JobDetails`信息持久化
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .forJob(job)
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();

            Trigger trigger2 = newTrigger()
                    .withIdentity("trigger2", "group1")
                    .forJob(job)
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3)
                            .repeatForever())
                    .build();
            // Tell quartz to schedule the job using our trigger
            scheduler.addJob(job,true); // 第二个参数`replace`决定了如果在调度器中已经存在一个具有相同标识的任务时，是否应该替换掉已有的任务。
            scheduler.scheduleJob(trigger);
            scheduler.scheduleJob(trigger2);
            TimeUnit.SECONDS.sleep(3);
            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
~~~

通常我们会为某个任务指定两个触发器，一个为自动触发`auto`，一个为手动触发`manual`。

由于自动触发是定时的，比如每天更新物流信息，每天只会更新一次。若数据出现错误，我们需要重新更新时，就需要用手动触发的方式。



多个任务的情况：

~~~java
public class QuartzTest_MultiJobDetail {

    public static void main(String[] args) {

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            JobDetail job = newJob(HelloJob.class)
                    .withIdentity("job1", "group1")
                    .build();

            JobDetail job2 = newJob(HelloJob.class)
                    .withIdentity("job2", "group1")
                    .build();
            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();

            Trigger trigger2 = newTrigger()
                    .withIdentity("trigger2", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(3)
                            .repeatForever())
                    .build();
            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job,trigger);
            scheduler.scheduleJob(job2,trigger2);
            TimeUnit.SECONDS.sleep(3);
            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
~~~

# 二、Trigger

Trigger有四个实现类：

- `SimpleTriggerImpl`
- `CronTriggerImpl`
- `CalondarIntervalTriggerImpl`
- `DailyTimeIntervalTriggerImpl`

其中，前两个用的比较多。尤其是第二个，能解决大部分需求。

`CronTriggerImpl`通过在`CronScheduleBuilder.cronSchedule()`中传入一个`CronExpression`，这是一个正则表达式，用于规定定时规则。

~~~java
Trigger trigger = newTrigger()
    .withIdentity("trigger1", "group1")
    .startNow()
    .withSchedule(
    CronScheduleBuilder.cronSchedule(""))
    .build();
~~~

其中，该正则表达式有6个固定位置和一个可选位置。格式为`"* * * * * * *"`

每个位置用空格分开，每个位置代表不同的含义，且有不同的允许填入符号和值，如下：

![](D:\学习笔记\quartz图片\CronExepression.jpg)

其中，年为选填参数。

`CronExpression`的规则比较复杂，接下来我们一一介绍这些符号的含义以及用法。

`*`字符用于指定所有值。例如，分钟字段中的`*`表示每分钟。

`?`只有日和星期字段允许使用。它用于指定“无特定值”。用于在两个字段之一中指定某项而不是另一个字段，可以防止冲突。比如当我们指定了日期时，不知道是星期几，为了防止冲突，可以用`?`代替。

`-`字符用于指定范围。例如，小时字段中的”10-12“表示“小时10、11和12”。

`,`字符用于指定其他值。例如，“星期几“字段中的” MON，WED，FRI“"表示”星期一，星期三和星期五的日子”。

`/`字符用于指定增量。例如，秒字段中的” 0/15“表示“秒0、15、30和45”。秒字段中的” 5/15“表示“秒5、20、35和50”。

在`/`之前指定`*`等同干指定0为开头的值。本质上，对于表达式中的每个字段，都有对应的值域。对于秒和分钟，数字范围为0到59。对于小时0到23，对于每月的0到31，以及对于月0到11（JAN到DEC）。`/`字符可以帮助您打开给定集合中的每个“第n个"值。因此，“月“字段中的“7/6"仅打开”7“月，并不意味着每6个月一次，因为6个月后已经是下一年了。请注意这一点。

`L`字符仅在“月“和“周”字段中允许使用。该字符是”last”的简写，但在两个字段中每个都有不同的含义,。例如，“月“字段中的值”L”表示”月的最后一天”，如非润年的1月31日，2月28日。如果单独在”星期几“字段中使用，则仅表示”7“或”SAT”(周六)。但是，如果在星期几字段中使用另一个值，则表示“该月的最后一个xx天“，例如，"6L“表示“该月的最后一个星期五”。还可以指定与该月最后一天的偏移量，例如”L-3“，这表示该月的倒数第三天。使用”L“选项时，不能指定范围，如”1-3L“。

`W`字符仅在"日"字段中允许使用。此字符用于指定最接近给定日期的工作日(星期一至星期五)例如，如果您要指定”15W"作为“日"字段的值，则含义是:"离月15日最近的工作日”。因此，如果15号是星期六，那么触发器将在14号星期五触发。如果15日是星期日，则触发器将在16日星期一触发。如果15号是星期二，那么它将在15号星期二触发。但是，如果您将”1W“指定为月份的值，而第1个是星期六，则触发器将在第3天，即星期一触发，而不是上个月的月底，即周五触发。因为它不会“跳过”一个月的边界。该符号同样不能指定范围。

还可将“L”和” W"字符组合为一个月中的一天的表达式，如”LW"，这表示“该月的最后一个工作日”。

`#`字符“仅在"星期"字段允许使用。此字符用于指定月份的“第n个"XXX天。例如，“星期几“字段中的”6#3”的值表示该月的第三个星期五(第6天=星期五，"#3”=该月的第三个星期五)。但是注意，如果您指定”#5”，并且该月的指定星期几中没有5个，则该月将不会触发。如果使用”#“字符，则“星期几”字段中只能有一个表达式("3#1,6#3”无效，因为有两个表达式)。

支持溢出范围-也就是说，左侧的数字大于右侧的数字。您可能会在晚上10点到凌晨2点做22-2。

# 三、传参

当我们需要传递一些参数给`Job`时，我们没办法通过构造器或者函数的参数传入，因为`Job`是`quartz`框架通过调用无参构造器帮我们创建的，他并不知道我们想要什么参数。因此，我们可以通过`JobDetail`和`Trigger`来传递参数。

对于`JobDetail`和`Trigger`而言，他们各自维护者一个`JobDataMap`，并且允许他们在构造过程往其中放入参数。

`JobExecutionContext`中可以获取到`JobDetail`和`Trigger`合并起来的`Map`，如果其中有相同的`key`，则以`Trigger`的优先

~~~java
public class QuartzTest_Data {

    public static void main(String[] args) {

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            JobDetail job = newJob(DataJob.class)
                    .withIdentity("job1", "group1")
                    .usingJobData("haha","I am JobDetail") // 传入参数
                    .build();

            // Trigger the job to run now, and then repeat every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .usingJobData("haha","I am Trigger") // 传入参数
                    .startNow()
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule("0/5 * * * * ? *")
                    )
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
            TimeUnit.SECONDS.sleep(10);
            scheduler.shutdown();

        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

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
// 结果
I am JobDetail
I am Trigger
I am Trigger
~~~

另一种传参方式：

我们可以通过在`DataJob`中定义变量，并提供`set()`方法来获取参数值。`quartz`在创建`Job`是会帮你检测`JobDetail`和`Trigger`有无对应的`key`，如果有则调用`set()`方法来注入对应的值。

~~~java
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
        System.out.println(haha); // I am Trigger
    }
}
~~~



# 四、注入Bean

如果我们使用了`Spring`框架，那么当我们需要在`Job`中注入`Bean`的时候，该怎么做呢？

~~~java
@Component
public class SpringBeanJob implements Job {
    @Autowired
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
    }
}
~~~

如果采用这种方式，是注入不了的。因为`Job`的是`quartz`帮我们创建的，`quartz`并不知道`@Autowired`是啥，也不知道`@Component`，不懂怎么注入`Bean`。

有两种解决方法：

## ① 纯`quartz`方案

添加一个工具类，用于获取`Spring`的上下文

~~~java
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext =applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
~~~

在`Job`中通过上下文获取对应类的`bean`

~~~java
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
                .getBean(StringUtils.uncapitalize(HelloService.class.getSimpleName())); // 获取bean
        System.out.println(helloService);
    }
}
~~~

由于使用了`Spring`，因此我们要更改启动方式

~~~java
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
~~~

## ② SpringBoot集成`quartz`

首先需要在`SpringBoot`中配置`quartz`

~~~xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
~~~

在`SpringBoot`的starter中会帮我们注册一个`scheduler`，因此我们可以直接注入。

之前提到，`Job`的是`quartz`帮我们创建的，`quartz`并不知道`@Autowired`是啥，也不知道`@Component`，不懂怎么注入`Bean`。所以`SpringBoot`提供了一个类供我们继承：`QuartzJobBean`。我们不再需要实现`Job`接口。这样一来，当我们创建`Job`时，`SpringBoot`便可以感知到。

~~~java
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
// 写一个配置类绑定JobDetail和Trigger
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
// 然后启动SpringBoot即可
@SpringBootApplication
public class QuartzLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzLearnApplication.class, args);
    }
}
~~~

并且`SpringBoot`还可以帮我们自动将`JobDetail`、`Trigger`与`scheduler`绑定，我们可以通过写一个配置类，并在配置类中定义对应的`Bean`来实现。

~~~java
@Configuration
public class JobConfig {
    @Bean
    public JobDetail springJobDetail(){
        return JobBuilder.newJob(SpringBeanJob2.class)
                .withIdentity("springJobDetail")
                .storeDurably()
                .build();
    }
    @Bean
    public Trigger springJobTrigger(){
        return TriggerBuilder.newTrigger()
                .forJob("springJobDetail")
                .startNow()
                .build();
    }
}
~~~

不过这种做法缺点比较明显，一个是当你的任务多起来的时候，`JobDetail`、`Trigger`与`scheduler`之间的关系不好处理，毕竟是`SpringBoot`帮你调度的。

第二个是定于`JobDetail`、`Trigger`的方法名不好起，因为`SpringBoot`是直接用你的方法名来起名字的。

# 五、quartz.properties

配置文件，用于配置各种参数。

具体的可以上官网查。

# 六、持久化

如果我们不做设置，`quartz`默认将运行过程中产生的数据放在内存中，比如产生的`Trigger`、`scheduler`、执行次数、成功案例数、失败案例数等信息。

为了方便查看当前项目的调度情况，以及后期追踪异常，我们通常会将这些信息做持久化，保存到数据库中。

`quartz`框架帮我们实现了这一点，并且`Spring`也支持`quartz`的持久化，我们只需简单的进行配置即可实现。

~~~yml
spring:
  application:
    name: quartz_learn
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/quartz_learn?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver

  quartz:
    jdbc:
      initialize-schema: always 
    job-store-type: jdbc
~~~

`job-store-type`表示存储方式采用`jdbc`而不是缓存。`initialize-schema`表示每次重新启动`quartz`都会重置数据库中的表格。

一旦我们启动`quartz`后，数据库中就会自行创建多张表格来存储信息，而每当有作业完成后，就会有新的记录被添加到各个表中。

此时quartz与我们的SpringBoot项目共用一个Datasource，毕竟我们是在spring中进行的配置。如果我们想为`quartz`配置独立的数据源，那就可以通过`@Bean`+`@QuartzDataSource`来声明

~~~java
@Configuration
public class JobConfig {
    @Bean
    @QuartzDataSource
    public DataSource qDatasource(){
        DriverManagerDataSource dataSource= new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setUrl("jdbc:mysql://localhost:3306/quartz_learn?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        return dataSource;
    }
}
~~~



