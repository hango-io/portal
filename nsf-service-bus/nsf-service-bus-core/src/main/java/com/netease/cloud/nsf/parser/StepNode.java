package com.netease.cloud.nsf.parser;

import com.netease.cloud.nsf.step.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 对Step的包装，能够获取根Step、父Step、所有子Step、前一个Step、后一个Step
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public class StepNode {
    // 当前节点
    private Step self;
    // 根节点
    private StepNode root;
    // 父节点
    private StepNode parent;
    // 所有子节点
    private List<StepNode> child;
    // 前一个兄弟节点
    private StepNode prev;
    // 后一个兄弟节点
    private StepNode next;

    private StepNode() {
    }

    public StepNode(Step step) {
        toNode(step, this, null);
        enrichNode(this, this);
    }

    /**
     * 将Step转为StepNode， 并填充当前节点、父节点、所有子节点
     * @param step
     * @param current
     * @param parent
     */
    private void toNode(Step step, StepNode current, StepNode parent) {
        current.self = step;
        current.parent = parent;
        List<StepNode> children = new ArrayList<>();
        if (Objects.nonNull(step.getChildSteps())) {
            for (Step stepItem : step.getChildSteps().getSteps()) {
                StepNode newChild = new StepNode();
                toNode(stepItem, newChild, current);
                children.add(newChild);
            }
        }
        current.child = children;
    }

    /**
     * 填充前兄弟节点，后兄弟节点
     * @param parent
     * @param root
     */
    private void enrichNode(StepNode parent, StepNode root) {
        parent.root = root;
        if (Objects.nonNull(parent.child()) && parent.child().size() != 0) {
            int size = parent.child().size();
            for (int i = 0; i < size; i++) {
                StepNode childItem = parent.child().get(i);
                enrichNode(childItem, root);
                if (i - 1 >= 0) {
                    childItem.prev = parent.child().get(i - 1);
                }
                if (i + 1 < size) {
                    childItem.next = parent.child().get(i + 1);
                }
            }
        }
    }

    public Step get() {
        return self;
    }

    public StepNode root() {
        return root;
    }

    public StepNode parent() {
        return parent;
    }

    public StepNode firstChild() {
        if (Objects.isNull(child) || child.size() == 0) {
            return null;
        } else {
            return child.get(0);
        }
    }

    public List<StepNode> child() {
        return child;
    }

    public StepNode prev() {
        return prev;
    }

    public StepNode next() {
        return next;
    }

    /**
     * 获得在当前节点前的所有兄弟节点的集合
     *
     * @return
     */
    public List<StepNode> prevAll() {
        List<StepNode> prevAll = new ArrayList<>();
        StepNode prev = this.prev;
        while (Objects.nonNull(prev)) {
            prevAll.add(prev);
            prev = prev.prev;
        }
        return prevAll;
    }

    /**
     * 获得在当前节点后的所有兄弟节点的集合
     *
     * @return
     */
    public List<StepNode> nextAll() {
        List<StepNode> nextAll = new ArrayList<>();
        StepNode next = this.next;
        while (Objects.nonNull(next)) {
            nextAll.add(next);
            next = next.next;
        }
        return nextAll;
    }
}
