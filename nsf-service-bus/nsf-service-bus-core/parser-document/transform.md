| 字段   | 含义   | 范围             | 备注 |    |
|:-----|:-----|:---------------|:---|:---|
| stepKind | step类型 | transform |    |    |
| property | step配置 |  | | |
| property.method | 可选setHeader、setProperty、setBody | | | |
| property.key | 当method为setHeader与setProperty，需要填写，表明是header或property的key | | | |
| property.expressionType | 可选simple、constant、jsonpath、xpath、xquery | | | |
| property.expression | 具体的表达式 | | | |


场景1：setHeader，当method为setHeader与setProperty，需要填写expressionKey
```json
{
  "id": 1,
  "stepKind": "transform",
  "property":{
    "key": "a",
    "method":"setHeader",
    "expressionType": "simple",
    "expression": "${header[a]}"
  }
}
```

场景2：setBody
```json
{
  "id": 1,
  "stepKind": "transform",
  "property":{
    "method":"setBody",
    "expressionType": "jsonpath",
    "expression": "$..store.book.length()"
  }
}
```

- expressionType:表明了使用什么类型的表达式，expression:表明了具体的表达式。如指定了expressionType=jsonpath，填写$.abc，则表明从body中解析json字符串$.abc的值
- key：当method为setHeader、setProperty时需要指定，表明header或property的key

表达式文档：
1. simple，官方文档：https://camel.apache.org/components/2.x/languages/simple-language.html

| Variable                                                 | Type     | Description                                                  |
| :------------------------------------------------------- | :------- | :----------------------------------------------------------- |
| camelId                                                  | String   | CamelContext名称                        |
| camelContext.**OGNL**                                    | Object   | CamelContext OGNL表达式. |
| exchange                                                 | Exchange | Exchange                                 |
| exchange.**OGNL**                                        | Object   | Exchange OGNL表达式. |
| exchangeId                                               | String   | exchange id                               |
| id                                                       | String   | message id                                         |
| body                                                     | Object   | body                                               |
| body.**OGNL**                                            | Object   | body OGNL expression. |
| bodyAs(*type*)                                           | Type     | 将body转换为目标结果，可能为空 |
| bodyAs(*type*).**OGNL**                                  | Object   | 将body转换为目标结果 执行OGNL表达式 转换结果可能为空 |
| mandatoryBodyAs(*type*)                                  | Type     | 将body转换为目标结果，结果要求不为空 |
| mandatoryBodyAs(*type*).**OGNL**                         | Object   | 将body转换为目标结果 执行OGNL表达式 |
| header.foo                                               | Object   | header获取值                                |
| header[foo]                                              | Object   | header获取值               |
| headers.foo                                              | Object   | header获取值                               |
| headers[foo]                                             | Object   | header获取值               |
| header.foo[bar]                                          | Object   | header获取map值 |
| header.foo.**OGNL**                                      | Object   | header获取值 执行OGNL表达式 |
| headerAs(*key*,*type*)                                   | Type     | 将header转换为目标key，和结果类型 |
| headers                                                  | Map      | 获取整个headers                    |
| property.foo                                             | Object   | property获取值    |
| exchangeProperty.foo                                     | Object   | property获取值    |
| property[foo]                                            | Object   | property获取值    |
| exchangeProperty[foo]                                    | Object   | property获取值    |
| property.foo.**OGNL**                                    | Object   | property获取值 执行OGNL表达式 |
| exchangeProperty.foo.**OGNL**                            | Object   | property获取值 执行OGNL表达式 |
| sys.foo                                                  | String   | 获取环境变量                                 |
| sysenv.foo                                               | String   | 获取环境变量               |
| exception                                                | Object   |获取exception.  Will fallback and grab caught exceptions (`Exchange.EXCEPTION_CAUGHT`) if the Exchange has any. |
| exception.**OGNL**                                       | Object   | 获取exception 执行OGNL表达式 |
| exception.message                                        | String   | 获取exception.message |
| exception.stacktrace                                     | String   | 获取exception.stacktrace |
| date:_command_                                           | Date     | Evaluates to a Date object. Supported commands are: **now** for current timestamp, **in.header.xxx** or **header.xxx** to use the Date object in the IN header with the key xxx. **out.header.xxx** to use the Date object in the OUT header with the key xxx. **property.xxx** to use the Date object in the exchange property with the key xxx. **file** for the last modified timestamp of the file (available with a File consumer). Command accepts offsets such as: **now-24h** or **in.header.xxx+1h** or even **now+1h30m-100**. |
| date:_command:pattern_                                   | String   | Date formatting using `java.text.SimpleDataFormat` patterns. |
| date-with-timezone:_command:timezone:pattern_            | String   | Date formatting using `java.text.SimpleDataFormat` timezones and patterns. |
| bean:_bean expression_                                   | Object   | Invoking a bean expression using the [Bean](https://camel.apache.org/components/2.x/bean-component.html) language. Specifying a method name you must use dot as separator. We also support the ?method=methodname syntax that is used by the [Bean](https://camel.apache.org/components/2.x/bean-component.html) component. |
| properties:_locations:key_                               | String   | **Deprecated (use properties-location instead) Camel 2.3:** Lookup a property with the given key. The `locations` option is optional. See more at Using PropertyPlaceholder. |
| properties-location:_http://locationskey[locations:key]_ | String   | **Camel 2.14.1:** Lookup a property with the given key. The `locations` option is optional. See more at Using PropertyPlaceholder. |
| properties:key:default                                   | String   | **Camel 2.14.1**: Lookup a property with the given key. If the key does not exists or has no value, then an optional default value can be specified. |
| routeId                                                  | String   | 获取路由ID |
| threadName                                               | String   | 获取线程名称 |
| ref:xxx                                                  | Object   | 获取指定bean |
| type:name.field                                          | Object   | **Camel 2.11:** To refer to a type or field by its FQN name. To refer to a field you can append .FIELD_NAME. For example you can refer to the constant field from Exchange as: `org.apache.camel.Exchange.FILE_NAME` |
| null                                                     | null     | **Camel 2.12.3:** represents a **null**                      |
| random_(value)_                                          | Integer  | *Camel 2.16.0:*returns a random Integer between 0 (included) and *value* (excluded) |
| random_(min,max)_                                        | Integer  | *Camel 2.16.0:*returns a random Integer between *min* (included) and *max* (excluded) |
| collate(group)                                           | List     | **Camel 2.17:** The collate function iterates the message body and groups the data into sub lists of specified size. This can be used with the Splitter EIP to split a message body and group/batch the splitted sub message into a group of N sub lists. This method works similar to the collate method in Groovy. |
| skip(number)                                             | Iterator | **Camel 2.19:** The skip function iterates the message body and skips the first number of items. This can be used with the Splitter EIP to split a message body and skip the first N number of items. |
| messageHistory                                           | String   | **Camel 2.17:** The message history of the current exchange how it has been routed. This is similar to the route stack-trace message history the error handler logs in case of an unhandled exception. |
| messageHistory(false)                                    | String   | **Camel 2.17:** As messageHistory but without the exchange details (only includes the route strack-trace). This can be used if you do not want to log sensitive data from the message itself. |

2. constant，静态内容，不支持任何占位符

3. jsonpath，用标准jsonpath expression解析body内容，官方文档：https://camel.apache.org/components/2.x/languages/jsonpath-language.html

4. xpath，用标准xpath expression解析body内容，官方文档：https://camel.apache.org/components/2.x/languages/xpath-language.html

5. xquery，用标准xquery expression解析body内容，官方文档：https://camel.apache.org/components/2.x/languages/xquery-language.html