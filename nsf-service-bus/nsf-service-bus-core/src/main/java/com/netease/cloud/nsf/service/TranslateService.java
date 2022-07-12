package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.step.Step;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public interface TranslateService {
    /**
     * 将step转换camel能够接受的xml配置
     * @param step step
     * @return
     */
    String translate(Step step);
}
