package org.hango.cloud.dashboard.webservice

class TemplateUtil {
    static getTemplateValue(String name, SoapCreatorContext ctx) {
        if (ctx.isArrayItem && ctx.ignoreArrayParent) {
            def path = "${ctx.path}".substring(0, "${ctx.path}".lastIndexOf('.'))
            return "{{ ${path} }}"
        }
        if (ctx.isArrayItem) ctx.isArrayItem = false
        return "{{ ${ctx.path}${name} }}"
    }

    static getBaseTemplateValue(SoapCreatorContext ctx) {
        def path = "${ctx.path}".substring(0, "${ctx.path}".lastIndexOf('.'))
        return "{{ ${path} }}"
    }

    static getAttrTemplateValue(String attr, SoapCreatorContext ctx) {
        return "{{?}}"
    }
}