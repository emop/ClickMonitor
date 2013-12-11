ClickMonitor
============

短网址点击监控, 从ClickGate实时取到点击信息，根据不同的检查规则，分析是否出现异常的点击。当发现由异常的点击，产生告警。让人工介入审核。

这只是一个简单的实时消息分析工具。

基本结构
========
*  Spout -- 借用Storm的概念，数据输入源。可以是一个文件，HTTP链接，pipline等
*  Bolt  -- 也是Storm里面的概念，处理从Spout 读到的数据。
*  Redies -- 数据存储层。

参数配置
=======

应用中的参数都是通过java的Property配置。 例如: java -Dtaodian.app_key=1, 支持的参数有：

*  http_port -- HTTP服务的端口号，默认：8082
*  taodian.api_id -- 淘点开放平台的，APP ID
*  taodian.api_secret -- 淘点开放平台的，APP SECRET
*  taodian.api_route -- API路由地址，默认：http://api.zaol.cn/api/route
*  log_level -- (debug,info,warn) 默认info
*  max_log_days -- 日志最多保留多少天，默认10.
*  core_worker_size -- 消息处理线程数量。
*  click_gate_url -- 配置实时日志流来源，例如: http://c.zaol.cn/log/?token=test&id=1

使用方式
=======
```

#java -jar ClickMonitor.jar -f short_url.log  -- 从一个文件分析日志

#java -jar ClickMonitor.jar -- 默认启动HTTP服务，从ClickGate 读取实时日志分析


```

本地编译和运行
===========

*  先需要Checkout， https://github.com/alphazero/jredis 编译一个Jar包，然后在编辑


```
#mvn install:install-file -DgroupId=org.jredis -DartifactId=jredis -Dversion=2.1.0 -Dpackaging=jar -Dfile=libs/jedis-2.1.0.jar -DgeneratePom=true

#mvn assembly:assembly  -- 编译一个完整的Jar包
```