| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | groovy |    |    |
| property | step配置 |  | | |
| property.groovyShell | groovy脚本内容 | | | |
| property.groovyImports | groovy需要额外import的包，多个包用逗号隔开 | | | |


场景1：执行groovy脚本
```json
{
  "id": 1,
  "stepKind": "groovy",
  "property":{
    "groovyShell":"def process(exchange){println 'hello'}",
    "groovyImports": "org.apache.camel.Exchange"
  }
}
```

- 参考文档：https://camel.apache.org/components/2.x/languages/groovy-language.html
- groovy内置对象有：context、camelContext、exchange、request、response、properties