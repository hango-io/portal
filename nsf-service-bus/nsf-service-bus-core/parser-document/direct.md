| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | direct |    |    |
| property | step配置 |  | | |
| property.mode | 可选from、to，from类型即触发器类型，to类型即连接类型 | | | |
| property.directIntegrationId | 当mode=to时需要填写，目标子流程的integration ID | | | |
| property.block | 可选，配置为true，当找不到对应的directEndpoint，会一直阻塞，直到对应directEndpoint流程启用。默认值为false | | | |
| property.failIfNoConsumers | 可选, 当对应directEndpoint consumer不存在时，是否报错. 默认true | | | |

请求时，没有对应的directEndpoint分为以下几种情况
- 当block=true、failIfNoConsumers=true时，请求会阻塞，直到directEndpoint启用
- 当block=true、failIfNoConsumers=false时，情况同上
- 当block=false、failIfNoConsumers=true时，请求会报错，当directEndpoint重新启用，请求成功
- 当block=false、failIfNoConsumers=false时，会忽略该step，继续执行

1. 触发器类型的direct
```json
{
  "id": 1,
  "stepKind": "direct",
  "property": {
    "mode": "from"
  }
}
```

2. 连接器类型的direct
```json
{
  "id": 1,
  "stepKind": "direct",
  "property": {
    "mode": "to",
    "directIntegrationId": "223344"
  }
}
```
