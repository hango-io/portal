package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.BaseParser;
import com.netease.cloud.nsf.parser.Parser;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.StepNode;
import org.dom4j.Element;
import org.quartz.CronExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/25
 **/
public class QuartzParser extends BaseParser implements Parser {
    // timer类型，可选simple、cron
    private static final String TYPE = "type";
    // type为simple类型时，执行的时间间隔
    private static final String INTERVAL = "interval";
    // type为cron类型时，提供cron表达式
    private static final String CRON_EXPRESSION = "cronExpression";
    // type为simple类型时，可指定执行次数，默认为-1，即不限制次数
    private static final String COUNT = "count";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        String type = node.get().getProperty().strictGet(TYPE, String.class);

        String name = String.format("%s_template", node.get().getId());
        String integrationId = getIntegrationId(node);
        String uri = String.format("quartz2://%s/%s", integrationId, name);
        Map<String, Object> uriOptions = new HashMap<>();
        if ("cron".equals(type)) {
            String cronExpression = node.get().getProperty().strictGet(CRON_EXPRESSION, String.class);
            // 对cron表达式进行校验
            if(!CronExpression.isValidExpression(cronExpression)){
                throw new RuntimeException(String.format("The cron expression [%s] validation fails.", cronExpression));
            }
            uriOptions.put("cron", cronExpression.trim());
            uriOptions.put("trigger.misfireInstruction", parserContext.getParserOptions().getQuartzCronTriggerMisfireInstructions());
        }
        if ("simple".equals(type)) {
            uriOptions.put("trigger.misfireInstruction", parserContext.getParserOptions().getQuartzSimpleTriggerMisfireInstructions());
            String interval = node.get().getProperty().strictGet(INTERVAL, String.class);
            // 设置为-1表示不限制执行次数
            Integer count = node.get().getProperty().getOrDefault(COUNT, Integer.class, -1);
            uriOptions.put("trigger.repeatInterval", Integer.parseInt(interval) * 1000L);
            // 不限次数执行
            if (count <= -1) {
                uriOptions.put("trigger.repeatCount", -1);
            } else {
                uriOptions.put("trigger.repeatCount", count - 1);
            }
        }
        uri = appendParametersToURI(uri, uriOptions);
        Element from = addElement(parent, node, "from");
        from.addAttribute("uri", uri);
        parseNext(parent, node, parserContext);
    }
}

