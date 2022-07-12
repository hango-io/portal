| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | timer |    |    |
| property | step配置 |  | | |
| property.name | timer名称 | | | |
| property.type | timer类型 | 可选cron、或者simple | | |
| property.cronExpression | 当type为cron类型时，填写cron表达式 | | | |
| property.interval | 当type为simple类型时，填写定时任务执行间隔，单位为秒 | | | |
| property.count（可选项）| 当type为simple类型时，可指定定时器执行的次数，默认值为-1，即不限制次数，为0，则不执行 | | | |
simple类型timer
```json
{
  "id": 1,
  "stepKind": "timer",
  "property":{
    "name":"timer1",
    "type": "simple",
    "interval": "10",
    "count": 10
  }
}
```

cron类型timer
```json
{
  "id": 1,
  "stepKind": "timer",
  "property":{
    "name":"timer1",
    "type": "cron",
    "cronExpression": "* * * * * ?",
    "count": -1
  }
}
```