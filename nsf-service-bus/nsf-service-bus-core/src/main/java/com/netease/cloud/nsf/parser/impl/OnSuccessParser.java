package com.netease.cloud.nsf.parser.impl;

import com.netease.cloud.nsf.parser.*;
import org.dom4j.Element;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/26
 **/
public class OnSuccessParser extends BaseParser implements Parser {
    // 执行成功时，long日志时，会打出对应的tag
    private static final String TAG = "tag";

    @Override
    public void parse(Element parent, StepNode node, ParserContext parserContext) {
        String tag = node.get().getProperty().strictGet(TAG, String.class);
        setHeader(parent, node, ParserConst.ON_SUCCESS_TAG, tag);
        Element to = addElement(parent, node, "to");
        String onSuccessBean = parserContext.getParserOptions().getOnSuccessBean();
        String uri = String.format("bean:%s", onSuccessBean);
        to.addAttribute("uri", uri);

        parseNext(parent, node, parserContext);
    }
}
