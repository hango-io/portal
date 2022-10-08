package org.hango.cloud.dashboard.meta;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.junit.Test;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DaoMetaUnitTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(DaoMetaUnitTest.class);

    public void testForAllDaoMeta() {
        logger.info("DaoMetaUnitTest ==== start ====");
        Configuration configuration = new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("org.hango.cloud.dashboard.apiserver"))
                .setScanners(new SubTypesScanner(false));
        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> allClass = reflections.getSubTypesOf(Object.class);
        for (Class<?> clazz : allClass) {
            if (clazz.getCanonicalName() != null && (clazz.getCanonicalName().endsWith("Info") || clazz.getCanonicalName().endsWith("Dto")
                    || clazz.getCanonicalName().endsWith("DTO"))) {
                ClassMethodExecutor executor = new ClassMethodExecutor(clazz);
                executor.executeAllMethod();
            }
        }
        System.out.println(projectId);
        logger.info("DaoMetaUnitTest ==== end ====");

    }

}
