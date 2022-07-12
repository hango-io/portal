| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | template |    |    |
| property | step配置 |  | | |
| property.template | 模板内容 | | | |


```json
{
  "id": 1,
  "stepKind": "template",
  "property":{
    "template":"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:getUserById xmlns:ns2=\"http://entity.nsf.cloud.netease.com\"><id>${headers['id']}</id></ns2:getUserById></soap:Body></soap:Envelope>"
  }
}
```