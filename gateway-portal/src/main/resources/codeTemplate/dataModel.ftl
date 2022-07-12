package ${packageName};

<#list importList as import>
    import ${import};
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/

public class ${modelName?cap_first} {
// 数据模型参数
<#list modelParamList as attr>
    private ${attr.type} ${attr.name};
</#list>

// 默认构造方法
public ${modelName?cap_first}() {
}

// Builder构造方法
private ${modelName?cap_first} (Builder builder) {
<#list modelParamList as attr>
    this.${attr.name} = builder.${attr.name};
</#list>
}

// 取设值方法
<#list modelParamList as attr>
    public void set${attr.name?cap_first}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    }

    public ${attr.type} get${attr.name?cap_first}() {
    return this.${attr.name};
    }

</#list>


@Override
public String toString() {
StringBuilder stringBuilder = new StringBuilder();
<#list modelParamList as attr>
    stringBuilder.append("${attr.name}:" + ${attr.name} + "\n");
</#list>

return stringBuilder.toString();
}

public static class Builder{
// 数据模型参数
<#list modelParamList as attr>
    private ${attr.type} ${attr.name};
</#list>

// 设值方法
<#list modelParamList as attr>
    public Builder ${attr.name}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    return this;
    }

</#list>

// build方法
public ${modelName?cap_first} build() {
return new ${modelName?cap_first}(this);
}

}


}