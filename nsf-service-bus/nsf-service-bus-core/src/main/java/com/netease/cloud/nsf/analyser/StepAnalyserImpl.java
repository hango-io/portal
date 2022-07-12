package com.netease.cloud.nsf.analyser;

import com.netease.cloud.nsf.step.Step;

import java.util.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/12
 **/
public class StepAnalyserImpl implements StepAnalyser {

    private final Step step;
    private List<Step> stepList;
    private Map<String, List<Step>> kindStepMap;
    private Map<String, Step> idStepMap;

    public StepAnalyserImpl(Step step) {
        this.step = step;
        this.stepList = new ArrayList<>();
        this.kindStepMap = new LinkedHashMap<>();
        this.idStepMap = new LinkedHashMap<>();
        analyze(step);
    }

    @Override
    public boolean containKind(String kind) {
        return kindStepMap.containsKey(kind) && kindStepMap.get(kind).size() > 0;
    }

    @Override
    public List<Step> getAll() {
        return stepList;
    }

    @Override
    public Step getById(String id) {
        return idStepMap.get(id);
    }

    @Override
    public List<Step> getByKind(String kind) {
        return kindStepMap.getOrDefault(kind, new ArrayList<>());
    }

    private void analyze(Step step) {
        this.stepList.add(step);
        this.idStepMap.put(step.getId(), step);
        this.kindStepMap.putIfAbsent(step.getStepKind(), new ArrayList<>());
        this.kindStepMap.get(step.getStepKind()).add(step);
        if (Objects.nonNull(step.getChildSteps()) && Objects.nonNull(step.getChildSteps().getSteps())) {
            for (Step childStep : step.getChildSteps().getSteps()) {
                analyze(childStep);
            }
        }
    }
}
