package com.netease.cloud.nsf.config;

import com.netease.cloud.nsf.dao.ResourceDao;
import com.netease.cloud.nsf.dao.meta.ResourceInfo;
import com.netease.cloud.nsf.service.TranslateService;
import com.netease.cloud.nsf.status.StatusConst;
import com.netease.cloud.nsf.status.StatusNotifier;
import com.netease.cloud.nsf.step.Step;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public class DefaultConfigStore implements ConfigStore {
    private TranslateService translateService;
    private TransactionTemplate template;
    private ResourceDao resourceDao;
    private StatusNotifier notifier;

    public DefaultConfigStore(TranslateService translateService, TransactionTemplate template, ResourceDao resourceDao, StatusNotifier notifier) {
        this.translateService = translateService;
        this.template = template;
        this.resourceDao = resourceDao;
        this.notifier = notifier;
    }

    @Override
    public void publish(String integrationId, Step step) {
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    ResourceInfo resourceInfo = toResource(integrationId, step);
                    if (resourceDao.contains(integrationId)) {
                        resourceDao.update(resourceInfo);
                    } else {
                        resourceDao.add(resourceInfo);
                    }
                    notifier.notifyStatus(StatusConst.RESOURCES_VERSION);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new RuntimeException("an error occur when publish resource. resource name=" + integrationId, throwable);
                }
            }
        });
    }

    @Override
    public void delete(String integrationId) {
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    resourceDao.delete(integrationId);
                    notifier.notifyStatus(StatusConst.RESOURCES_VERSION);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new RuntimeException("an error occur when publish resource. resource name=" + integrationId, throwable);
                }
            }
        });
    }


    private ResourceInfo toResource(String integrationId, Step step) {
        ResourceInfo info = new ResourceInfo();
        String body = translateService.translate(step);
        info.setBody(body);
        info.setVersion(String.valueOf(body.hashCode()));
        info.setName(integrationId);
        info.setCreateTime(new Date().toString());
        info.setLabels("{}");
        return info;
    }
}
