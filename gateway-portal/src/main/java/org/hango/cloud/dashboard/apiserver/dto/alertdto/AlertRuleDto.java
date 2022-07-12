package org.hango.cloud.dashboard.apiserver.dto.alertdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/5
 */
public class AlertRuleDto {


    @JSONField(name = "Name")
    @NotEmpty
    private String name;


    @JSONField(name = "Enable")
    private boolean enable;


    @JSONField(name = "Expression")
    @Valid
    @NotNull
    private AlertRuleDtoExpr expression;

    @JSONField(name = "Expr")
    private String exprStr;


    @JSONField(name = "Annotations")
    @NotNull
    private Map<String, Object> annotations;


    @JSONField(name = "NotifyMethod")
    private List<String> notifyMethod;


    @JSONField(name = "Contacts")
    private List<String> contacts;


    @JSONField(name = "Status")
    private String status;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlertRuleDtoExpr getExpression() {
        return expression;
    }

    public void setExpression(AlertRuleDtoExpr expression) {
        this.expression = expression;
    }

    public String getExprStr() {
        return exprStr;
    }

    public void setExprStr(String exprStr) {
        this.exprStr = exprStr;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    public List<String> getNotifyMethod() {
        return notifyMethod;
    }

    public void setNotifyMethod(List<String> notifyMethod) {
        this.notifyMethod = notifyMethod;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
