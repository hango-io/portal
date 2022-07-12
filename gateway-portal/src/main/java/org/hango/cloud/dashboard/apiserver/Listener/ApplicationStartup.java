//package com.netease.cloud.ncegdashboard.apiserver.Listener;
//
//import com.netease.cloud.ncegdashboard.apiserver.service.IPluginInfoService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
///**
// * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
// * @Date: 创建时间: 2019/4/11 下午3:15.
// */
////@Component
//public class ApplicationStartup implements CommandLineRunner {
//    protected static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);
//
//    @Autowired
//    private IPluginInfoService pluginInfoService;
//
//    @Override
//    public void run(String... strings) throws Exception {
//        new Thread("PluginCountingNumberManagerPoller") {
//            public void run() {
//                while (true) {
//                    try {
//                        sleep(2 * 60 * 1000);
//                        logger.info("开始查询各插件的调用次数......");
//                        pluginInfoService.updatePluginInfoCallNumber();
//                    } catch (Exception e) {
//                        logger.error("查询各插件的调用次数时发生异常", e);
//                    }
//                }
//            }
//        }.start();
//    }
//}
