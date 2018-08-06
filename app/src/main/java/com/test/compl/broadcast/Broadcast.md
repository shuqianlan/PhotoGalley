
- 多个接受者 (BroadCastReceiver) (观察者+订阅者)

> 实现系统内全局性的消息传递.

receiver
permission
<intent-filter>-action:->XXXX

- BroadcastReceiver的动态创建及自我的权限管理, 因其为开放性就有需要关注权限的闭源操作.
- <permission android:name="packagename.PRIVATE protectionLeve(安全级别)

protectionLevel

```
	#注册带权限的广播事件.
	IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
	getActivity().registerReceiver(mOnShowNotification, intentFilter, PollService.PREM_PRIVATE, null);
```
* normal:    用于阻止应用进行危险操作。例如访问个人隐私数据库等
* dangerous: 用于normal安全级别控制以外的任何危险操作等包含任何给用户带来麻烦的行为
* signature: 外部APP使用则需相同的key做签名认证 | 权限授予时，系统不会通知用户，通常用于系统内部
* signatureOrSystem: 类似signature，但该授权针对Android系统镜像中的所有包授权，用于系统镜像内应用间的访问。

- 广播序列

可接收处理结果/及Receiver之间的相互影响.

- 可将Notification序列化传递给接受者，在正确接收到通知时核定是否显示该通知.(NotificationReceiver.java) | 设置优先级 | 过滤action | 清单文件中声明<四大组件>

- receiver与长时间运行任务

* 启动服务
* Async(); 返回一个PendingIntent来进行，不过限制较大。

- EventBus<应用内的消息事件广播>

：提供一个应用内的部件可以订阅的共享总线或数据流。事件一旦发布到总线上，各订阅部件就会被激活并执行响应的回调代码.

- * EventBus

> https://www.cnblogs.com/bugly/p/5475034.html | EventBus事件简述.

- * RxJava

订阅者/发布者模式.