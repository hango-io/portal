package com.netease.cloud.nsf.resource;

import nsb.route.ResourceOuterClass;
import nsb.route.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/19
 **/
public class DefaultResourceManager implements ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceManager.class);

    private Service.Resources resources;
    private List<ResourceMonitor> monitors;
    private ExecutorService singleThread = Executors.newSingleThreadExecutor();

    public DefaultResourceManager(List<ResourceMonitor> monitors) {
        this.monitors = monitors;
        this.resources = Service.Resources.newBuilder().setNonce("").build();
    }

    @Override
    public synchronized void setResources(Service.Resources resources) {
        if (Objects.isNull(resources) || Objects.isNull(resources.getResourcesList())) return;
        for (ResourceOuterClass.Resource resource : resources.getResourcesList()) {
            if (Objects.isNull(resource.getMetadata().getName()) || Objects.isNull(resource.getMetadata().getVersion())) {
                throw new RuntimeException("resource name or version cannot be empty.resource body=" + resource.getBody());
            }
        }
        compareAndNotify(this.resources, resources, monitors);
        this.resources = resources;
    }

    @Override
    public synchronized void clearResources() {
        this.resources = null;
    }

    private void compareAndNotify(Service.Resources oldRs, Service.Resources newRs, List<ResourceMonitor> monitors) {
        Difference difference = compare(oldRs.getResourcesList().toArray(new ResourceOuterClass.Resource[0]),
                newRs.getResourcesList().toArray(new ResourceOuterClass.Resource[0]));
        logger.info("GPpc: compare resources. old [size={} nonce={}] new [size={} nonce={}] add={} update={} delete={}", getSize(oldRs.getResourcesList()), oldRs.getNonce(), getSize(newRs.getResourcesList()), newRs.getNonce(), difference.add.size(), difference.update.size(), difference.delete.size());
        for (ResourceMonitor monitorItem : monitors) {
            for (ResourceOuterClass.Resource item : difference.add) {
                singleThread.execute(() -> monitorItem.onAdd(item));
            }
            for (ResourceOuterClass.Resource[] item : difference.update) {
                singleThread.execute(() -> monitorItem.onUpdate(item[0], item[1]));
            }
            for (ResourceOuterClass.Resource item : difference.delete) {
                singleThread.execute(() -> monitorItem.onDelete(item));
            }
        }
    }

    private Difference compare(ResourceOuterClass.Resource[] olds, ResourceOuterClass.Resource[] news) {
        Comparator<ResourceOuterClass.Resource> comparator = Comparator.comparing(o -> o.getMetadata().getName());
        Arrays.sort(olds, comparator);
        Arrays.sort(news, comparator);

        Difference diff = new Difference();
        int i = 0, j = 0;
        int oldSize = olds.length;
        int newSize = news.length;
        while (i < oldSize && j < newSize) {
            if (Objects.equals(olds[i].getMetadata().getName(), news[j].getMetadata().getName())) {
                // update
                if (!Objects.equals(olds[i].getMetadata().getVersion(), news[j].getMetadata().getVersion())) {
                    diff.update.add(new ResourceOuterClass.Resource[]{olds[i], news[j]});
                }
                i++;
                j++;
            } else {
                if (olds[i].getMetadata().getName().compareTo(news[j].getMetadata().getName()) > 0) {
                    // add
                    diff.add.add(news[j]);
                    j++;
                } else {
                    // delete
                    diff.delete.add(olds[i]);
                    i++;
                }
            }
        }
        // delete
        while (i < oldSize) {
            diff.delete.add(olds[i]);
            i++;
        }
        // add
        while (j < newSize) {
            diff.add.add(news[j]);
            j++;
        }
        return diff;
    }

    private int getSize(List list) {
        if (Objects.isNull(list)) return 0;
        return list.size();
    }

    private static class Difference {
        private final List<ResourceOuterClass.Resource> add = new ArrayList<>();
        private final List<ResourceOuterClass.Resource[]> update = new ArrayList<>();
        private final List<ResourceOuterClass.Resource> delete = new ArrayList<>();
    }
}
