| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | delay |    |    |
| property | step配置 |  | | |
| property.delayType | 可选constant、simple | | | |
| property.delayDuration | 当delayType=constant时，填写delay时长，单位为ms | | | |
| property.delayExpression | 当delayType=simple时，填写simple表达式 | | | |

1. delay固定时间
```json
{
  "id": 1,
  "stepKind": "delay",
  "property": {
    "delayType": "constant",
    "delayDuration": 2000
  }
}
```

2. 根据simple表达式，获取delay时间
```json
{
  "id": 1,
  "stepKind": "delay",
  "property": {
    "delayType": "simple",
    "delayExpression": "${headers.delayTimeMs}"
  }
}
```