| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | loop |    |    |
| property | step配置 |  | | |
| property.loopMode | 可选repeat、doWhile | | | |
| property.copy | 如果为true,则每次循环时，都会使用循环exchange副本，每次循环的exchange都会相同；如果为false，则每次循环都使用同一份上下文，默认值为false | | | |
| property.repeatCount | 当loopMode=repeat时，配置重复执行次数 | | | |
| property.predicateType | 当loopMode=doWhile时，可选simple、groovy | | | |
| property.simpleExpression | 当predicateType=simple时，填写expression表达式 | | | |
| property.groovyShell | 当predicateType=groovy时，填写groovy脚本 | | | |
| property.subIntegrationType | 可选，当配置了subIntegration则必须要配置，可选项有:1. direct 2. seda. direct会将子流程与当前流程合并为一条流程. seda则是将子流程作为新的流程请求 | | | |
| property.subIntegration | 可选，将循环体配置为子流程，若配置该选项，则不会继续parse childStep，而使用childStep作为循环体| | | |
| property.failIfNoConsumers | 可选, 当对应directEndpoint consumer不存在时，是否报错. 默认true | | | |
| property.block | 可选，当subIntegrationType=direct时可配， 配置为true，当找不到对应的directEndpoint，会一直阻塞，直到对应directEndpoint流程启用。默认值为true | | | |
| property.blockWhenFull | 可选，当subIntegrationType=seda时可配，当队列满时，是否block直到消息被结束. 默认false将抛出异常. 默认false. | | | |
| property.offerTimeout | 可选，当subIntegrationType=seda时可配，当blockWhenFull = true时，可配置offer block timeout. 可以通过配0或负数，disable该选项. | | | |
| property.waitForTaskToComplete | 可选，当subIntegrationType=seda时可配，是否等待task结束，可选项有Never、IfReplyExpected、Always. 默认为IfReplyExpected，将根据触发器类型来决定是异步还是同步返回. 默认IfReplyExpected | | | |
| property.timeout | 当mode=to时需要填写，等待异步任务完成的超时时间，可以设置0或负数，来disable该选项 | | | |
| property.discardIfNoConsumers | 可选，当subIntegrationType=seda时可配，当指定consumer不存在时，是否忽略. 和failIfNoConsumers同时只能有一个选项生效. 默认false | | | |

请求时，没有对应的directEndpoint分为以下几种情况
- 当block=true、failIfNoConsumers=true时，请求会阻塞，直到directEndpoint启用
- 当block=true、failIfNoConsumers=false时，情况同上
- 当block=false、failIfNoConsumers=true时，请求会报错，当directEndpoint重新启用，请求成功
- 当block=false、failIfNoConsumers=false时，会忽略该step，继续执行

loop循环中，有两个默认的properties
- CamelLoopSize: 总共循环次数，如果为条件循环模式，则不存在该参数
- CamelLoopIndex: 当前循环Index

1. 使用固定重复次数场景
```json
{
  "id": 1,
  "stepKind": "loop",
  "property": {
    "loopMode": "repeat",
    "repeatCount": 5
  },
  "childSteps": {
    "steps": []
  }
}
```

2. 使用doWhile场景，同时根据simple表达式决定循环次数
```json
{
  "id": 1,
  "stepKind": "loop",
  "property": {
    "loopMode": "doWhile",
    "predicateType": "simple",
    "simpleExpression": "${body.length} <= 5"
  },
  "childSteps": {
    "steps": []
  }
}
```

3. 使用doWhile场景，同时根据groovy表达式决定循环次数
```json
{
  "id": 1,
  "stepKind": "loop",
  "property": {
    "loopMode": "doWhile",
    "predicateType": "groovy",
    "groovyShell": "return request.headers.index < 10"
  },
  "childSteps": {
    "steps": []
  }
}
```

4. 使用嵌套子流程，子流程Id=1546498
```json
{
  "id": 1,
  "stepKind": "loop",
  "property": {
    "loopMode": "doWhile",
    "predicateType": "groovy",
    "groovyShell": "return request.headers.index < 10",
    "subIntegration": "1546498"
  }
}
```
