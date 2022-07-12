| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | openApi |    |    |
| property | step配置 |  | | |
| property.uri | 暴露的path | | | |
| property.method | 暴露的method | | | |
| property.transferException | 可选，是否将流程中的异常直接返回给调用方，默认为false | | | |
| property.muteException | 可选，如果流程执行失败，responseBody是否包含异常堆栈信息，默认为true | | | |

场景1：暴露一个openApi接口
```json
{
  "id": 1,
  "stepKind": "openApi",
  "property":{
    "uri":"/abc",
    "method": "GET"
  }
}
```

- 该step只指定了path和method，host和port在worker配置文件里默认指定