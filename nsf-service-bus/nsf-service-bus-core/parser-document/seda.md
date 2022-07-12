| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | seda |    |    |
| property | step配置 |  | | |
| property.mode | 可选from、to，from类型即触发器类型，to类型即连接类型 | | | |
| property.size | 当mode=from时可配，队列长度, 默认1000 | | | |
| property.concurrentConsumers | 当mode=from时可配，consumer并行处理的数量，默认1 | | | |
| property.multipleConsumers | 当mode=from时可配，是否允许多consumer. 如果为true，可以使用SEDA实现消息订阅发布模式. 你可以发布一条消息而多个consumer都会接收到该消息的副本， 默认false | | | |
| property.pollTimeout | 当mode=from时可配，当超时发生时，consumer可以检查是否继续运行. 设置较小的值可以使consumer更快关闭.单位ms. 默认1000 | | | |
| property.purgeWhenStopping | 当mode=from时可配，当路由停止时，是否清空队列. 该选项能加快路由关闭速度. 默认false | | | |
| property.sedaIntegrationId | 当mode=to时需要填写，目标子流程的integration ID | | | |
| property.blockWhenFull | 当mode=to时需要填写，当队列满时，是否block直到消息被结束. 默认false将抛出异常. 默认false | | | |
| property.offerTimeout | 当mode=to时需要填写，当blockWhenFull = true时，可配置offer block timeout. 可以通过配0或负数，disable该选项 | | | |
| property.waitForTaskToComplete | 可选，当subIntegrationType=seda时可配，是否等待task结束，可选项有Never、IfReplyExpected、Always. 默认为IfReplyExpected，将根据触发器类型来决定是异步还是同步返回. 默认IfReplyExpected | | | |
| property.timeout | 当mode=to时需要填写，等待异步任务完成的超时时间，可以设置0或负数，来disable该选项 | | | |
| property.discardIfNoConsumers | 可选，当subIntegrationType=seda时可配，当指定consumer不存在时，是否忽略. 和failIfNoConsumers同时只能有一个选项生效. 默认false | | | |
| property.failIfNoConsumers | 当mode=to时需要填写，当对应directEndpoint consumer不存在时，是否报错. 默认true | | | |

1. 触发器类型的seda
```json
{
  "id": 1,
  "stepKind": "seda",
  "property": {
    "mode": "from"
  }
}
```

2. 连接器类型的seda
```json
{
  "id": 1,
  "stepKind": "seda",
  "property": {
    "mode": "to",
    "sedaIntegrationId": "223344"
  }
}
```
