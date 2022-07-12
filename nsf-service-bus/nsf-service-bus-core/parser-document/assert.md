| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | assert |    |    |
| property | step配置 |  | | |
| property.conditionType | 条件判断类型 | 可选range、groovy | | |
| property.range | 当conditionType为ranges时，指定正常返回码的区间，在区间外，则会执行action | | | |
| property.groovyImports | groovy需要额外import的包，多个包用逗号隔开 | | | |
| property.shell | 当conditionType为groovy时，填写自定义脚本判断结果是否正常，执行结果为false时，会执行action | | | |
| property.action | 当条件为false时，执行的操作 | return | | | |
| property.body（非必填） | 当action为return时，需要返回的body | | | | |
| property.returnType（非必填） | 当action为return,配置return返回类型是正常立即返回，还是结果不符合预期中断返回 |可选successReturn、failureReturn 默认为failureReturn | | | |
场景1：使用range判断http返回码范围
```json
{
  "id": 1,
  "stepKind": "assert",
  "property":{
    "conditionType":"range",
    "range": "200-299",
    "action": "return",
    "returnType": "failureReturn",
    "body": "An exception occurred while executing the HTTP request. response body: ${body}"
  }
}
```

场景2：使用groovy判断执行是否成功
```json
{
  "id": 1,
  "stepKind": "assert",
  "property":{
    "conditionType":"groovy",
    "groovyImports": "groovy.json.*",
    "shell": "return request.getHeader(Exchange.HTTP_RESPONSE_CODE) == 200",
    "action": "return",
    "returnType": "failureReturn",
    "body": "An exception occurred while executing the HTTP request. response body: ${body}"
  }
}
```