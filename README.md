# 熔断器Servlet版本
## 简介
- 网上大部分熔断的都是针对SpringBoot，SpringCloud环境下使用熔断器，当遇到非这种环境也需要使用熔断的情况，这些代码应该可以帮助到你
- 主要基于[Netflix/Hystrix](https://github.com/Netflix/Hystrix) 进行控制

##将以下过滤器内容添加到web.xml进行熔断的控制
```lua
  <filter>
    <display-name>HystrixRequestServletFilter</display-name>
    <filter-name>HystrixRequestServletFilter</filter-name>
    <filter-class>com.luohh.servlet.filter.HystrixRequestServletFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>HystrixRequestServletFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
```
##将以下过滤器内容添加到web.xml进行查看熔断统计，访问路径:http://localhost:port/项目路径/hystrix-statistics
```lua
  <servlet>
	<servlet-name>HystrixRequestStatisticsServlet</servlet-name>
		<servlet-class>com.luohh.servlet.HystrixRequestStatisticsServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>HystrixRequestStatisticsServlet</servlet-name>
	<url-pattern>/hystrix-statistics</url-pattern>
  </servlet-mapping>
```
##可修改hystrix-config.properties文件控制，主要用来控制的参数配置:
- 主要有下面5种类型的属性配置：
- execution配置
- 该配置前缀为 hystrix.command.default
- execution.isolation.strategy ：该属性用来设置执行的隔离策略，有如下二个选项：
- THREAD：通过线程池隔离的策略，在独立线程上执行，并且他的并发限制受线程池中线程数量的限制（默认）
- SEMAPHONE：通过信号量隔离的策略，在调用线程上执行，并且他的并发限制受信号量计数的限制。
- execution.isolation.thread.timeoutInMilliseconds：该属性用来配置 HystrixCommand 执行的超时时间，单位为毫秒，默认值 1000 ，超出此时间配置，Hystrix 会将该执行命令为 TIMEOUT 并进入服务降级处理逻辑
- execution.timeout.enabled：该属性用来配置 HystrixCommand 执行是否启动超时时间，默认值 true，如果设置为 false，则 execution.isolation.thread.timeoutInMilliseconds 属性的配置将不起作用
- execution.isolation.thread.interruptOnTimeout：该属性用来配置当 HystrixCommand 执行超时的时候，是否需要将他中断，默认值 true
- execution.isolation.semaphore.maxConcurrentRequests：当隔离策略使用信号量时，该属性用来配置信号量的大小，当最大并发请求数达到该设置值，后续的请求将会被拒绝

- fallback配置
- 该配置前缀为 hystrix.command.default
- fallback.enabled：该属性用来设置服务降级策略是否启用，默认值 true ，如果设置为false，当请求失败或拒绝发生时，将不会调用 HystrixCommand.getFallback() 来执行服务降级逻辑

- circuitBreaker 配置
- 该配置前缀为 hystrix.command.default
- circuitBreaker.enabled：该属性用来确定当服务请求命令失败时，是否使用断路器来跟踪其健康指标和熔断请求，默认值 true
- circuitBreaker.requestVolumeThreshold：该属性用来设置在滚动时间窗中，断路器的最小请求数。例如：默认值 20 的情况下，如果滚动时间窗（默认值 10秒）内仅收到19个请求，即使这19个请求都失败了，断路器也不会打开。
- circuitBreaker.sleepWindowInMilliseconds：该属性用来设置当断路器打开之后的休眠时间窗。默认值 5000 毫秒，休眠时间窗结束之后，会将断路器设置为"半开"状态，尝试熔断的请求命令，如果依然失败就将断路器继续设置为"打开"状态，如果成功就设置为"关闭"状态。
- circuitBreaker.errorThresholdPercentage：该属性用来设置断路器打开的错误百分比条件。例如，默认值为 50 的情况下，表示在滚动时间窗中，在请求数量超过 circuitBreaker.requestVolumeThreshold 阈值的请求下，如果错误请求数的百分比超过50，就把断路器设置为"打开"状态，否则就设置为"关闭"状态。
- circuitBreaker.forceOpen：该属性用来设置断路器强制进入"打开"状态，会拒绝所有请求，该属性优先于 circuitBreaker.forceClosed
- circuitBreaker.forceClosed：该属性用来设置断路器强制进入"关闭"状态，会接收所有请求。

- metrics 配置
- 该配置属性与HystrixCommand 和 HystrixObservableCommand 执行中捕获指标信息有关，配置前缀为 hystrix.command.default
- metrics.rollingStats.timeInMilliseconds：该属性用于设置滚动时间窗的长度，单位毫秒，该时间用于断路器判断健康度时需要收集信息的持续时间，默认值 10000 。断路器值啊收集指标信息时候会根据设置的时间窗长度拆分成多个"桶"来累计各度量值，每个"桶"记录了一段时间内的采集指标。
- metrics.rollingStats.numBuckets：该属性用来设置滚动时间窗统计指标信息时，划分"桶"的数量，默认值 10 。 metrics.rollingStats.timeInMilliseconds 参数的设置必须能被该参数整除，否则将抛出异常。
- metrics.rollingPercentile.enabled：该属性用来设置对命令执行的延迟是否使用百分位数来跟踪和计算，默认值 true ，如果设置为 false 那么所有概要统计都将返回 -1
- metrics.rollingPercentile.timeInMilliseconds：该属性用来设置百分位统计的滚动窗口的持续时间，单位：毫秒，默认值 60000
- metrics.rollingPercentile.numBuckets：该属性用来设置百分位统计窗口中使用"桶"的数量，默认值 6
- metrics.rollingPercentile.bucketSize：该属性用来设置在执行过程中每个"桶"中保留的最大执行次数，如果在滚动时间窗内发生超该设定值的执行次数，就从最初的位置开始重写，例如：设置为 100，滚动窗口为 10 秒，若在10秒内一个"桶"中发生了500次执行，那么该"桶"中只保留最后的100次执行的统计，默认值 100
- metrics.healthSnapshot.intervalInMilliseconds：该属性用来设置采集影响断路器状态的健康快照（请求的成功、错误百分比）的间隔等待时间，默认值 500

- requestContext 配置
- 该配置前缀为 hystrix.command.default
- requestCache.enabled：该属性用来配置是否开启请求缓存
- requestLog.enabledg：该属性用来设置 HystrixCommand 的执行和事件是否打印日志到 HystrixRequestLog 中，默认值 true

- collapser 配置
- 该配置前缀为 hystrix.collapser.default
- maxRequestsInBatch：该属性用来设置一次请求合并批处理允许的最大请求数量，默认值 Integer.MAX_VALUE
- timerDelayInMilliseconds：该属性用来设置批处理过程中每个命令延迟的时间，单位毫秒，默认值 10
- requestCache.enabled：该属性用来设置批处理过程中是否开启请求缓存，默认值 true

- threadPool 配置
该配置前缀为 hystrix.threadpool.default
- coreSize：该属性用来设置执行命令线程池的核心线程数，该值也就是命令执行的最大并发量，默认值 10
- maxQueueSize：该属性用来设置线程池的最大队列大小，当设置为 -1 时，线程池将使用 SynchronousQueue 实现的队列，否则使用 LinkedBlockingQueue 实现的队列，默认值 -1
- queueSizeRejectionThreshold ：该属性用来为队列设置拒绝阈值，即使队列没有到达最大值也能拒绝请求，该属性主要对 LinkedBlockingQueue 队列的补充，默认值 5，当 maxQueueSize 属性为 -1 时候，该属性无效
- metrics.rollingPercentile.timeInMilliseconds：该属性用来设置线程池统计的滚动窗口的持续时间，单位：毫秒，默认值 10000
- metrics.rollingPercentile.numBuckets：该属性用来设置线程池统计窗口中使用"桶"的数量，默认值 10



