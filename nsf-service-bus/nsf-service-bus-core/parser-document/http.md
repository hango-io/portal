| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | http |    |    |
| property | step配置 |  | | |
| property.uri | uri | | | |
| property.method | method | | | |
| property.protocol | 协议类型，默认http | http、https | | |
| property.headers | (可选) 请求headers，以key value形式 | | | |
| property.queryParams | (可选) 请求query，以key value形式 | | | |
| property.body | (可选) 请求body，若填写会覆盖当前body | | | |
| property.copyHeaders | （可选）是否将请求前的header复制到请求后，如果为false，则执行完http请求，header为response header。 默认为true | | | |
| property.clearHeaders | （可选）是否清理与请求无关的headers，默认为true | | | |
| property.convertBodyToString | （可选）是否将response input stream转为string，默认为true | | | |
| property.connectionRequestTimeout | （可选）连接请求超时默认5s | | | |
| property.connectTimeout | （可选）连接超时默认30s | | | |
| property.socketTimeout | (可选)socket超时默认30s | | | |

```json
{
  "id": 1,
  "stepKind": "http",
  "property": {
    "uri": "localhost:8080",
    "method": "POST",
    "protocol": "http",
    "headers": [
      {
        "key": "a",
        "value": "b"
      },
      {
        "key": "c",
        "value": "d"
      }
    ],
    "queryParams": [
      {
        "key": "query1",
        "value": "b"
      },
      {
        "key": "query2",
        "value": "d"
      }
    ],
    "body": "kkk"
  }
}
```

- header、query、body可使用simple expression，simple expression使用可参考文档：https://camel.apache.org/components/2.x/languages/simple-language.html