package org.hango.cloud.dashboard.apiserver.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hango.cloud.dashboard.apiserver.service.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class DistLockAdvice implements Ordered {

    private static Logger logger = LoggerFactory.getLogger(DistLockAdvice.class);

    private static String DIST_LOCK_PREFIX = "INTERLAYER_DIST_LOCK_PREFIX_";

    @Autowired
    private IRedisService redisService = null;

    @Around("@annotation(lock)")
    public Object doLock(ProceedingJoinPoint pjp, DistLock lock) throws Throwable {
        Long expire = null;
        try {
            expire = Long.valueOf(lock.expire());
        } catch (Exception e) {
            logger.error("exception", e);
            expire = null;
        }
        if (expire == null || expire <= 0) {
            logger.warn("Dist lock expire time is illegal. expire = " + expire);
            return null;
        }

        Signature sig = pjp.getSignature();
        String lockKey = DIST_LOCK_PREFIX + sig.getDeclaringTypeName() + sig.getName() + expire;
//        boolean succ = false;
//        try {
//            succ = redisService.setValueIfAbsent(lockKey, true);
//        } catch (Exception e) {
//            return null;
//        }
//        if (succ) {
//            logger.info("acquire dist lock success. lockKey = " + lockKey);
//            if (redisService.expire(lockKey, expire * 1000L)) {
//                logger.info("set ttl success. lockKey = " + lockKey + ", ttl = " + expire + "s");
//                return pjp.proceed();
//            } else {
//                logger.warn("set ttl fail! lockKey = " + lockKey + ", ttl = " + expire + "s");
//                redisService.deleteKey(lockKey);
//            }
//        } else {
//            logger.info("acquire dist lock fail!. lockKey = " + lockKey);
//        }
//        return null;


        try {
//            Long value = redisService.setLock(lockKey, true, expire * 1000L);
//            if (value.equals(1L)){
            if (redisService.getLock(lockKey, true, expire * 1000L)) {
                logger.info("acquire dist lock success. lockKey = " + lockKey + ", ttl = " + expire + "s");
                return pjp.proceed();
            } else {
                logger.warn("acquire dist lock fail! lockKey = " + lockKey);
            }
        } catch (Exception e) {
            logger.warn("acquire dist lock fail! lockKey = " + lockKey + ",exception:" + e);
        }
        return null;
    }

    public int getOrder() {
        return 200;
    }
}
