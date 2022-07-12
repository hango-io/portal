package com.netease.cloud.nsf.analyser;

import com.netease.cloud.nsf.step.Step;

import java.util.List;

/**
 * Step解析器接口。 提供一组解析Step的接口方法。
 * 1. 判断是否包含某个Kind。
 * 2. 获取一个Step中的所有Step(包括子Step)。
 * 3. 根据Id获取Step中嵌套包含的Step。
 * 4. 根据特定kind获取所有Step。
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/12
 **/
public interface StepAnalyser {
    /**
     * 判断是否包含某个Kind
     *
     * @param kind kind
     * @return true or false
     */
    boolean containKind(String kind);

    /**
     * 获取一个Step中的所有Step(包括子Step)
     *
     * @return Step列表
     */
    List<Step> getAll();

    /**
     * 根据Id获取Step中嵌套包含的Step
     *
     * @param id stepId
     * @return Step
     */
    Step getById(String id);

    /**
     * 根据特定kind获取所有Step
     *
     * @param kind kind
     * @return Step列表
     */
    List<Step> getByKind(String kind);
}
