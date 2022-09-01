package org.hango.cloud.dashboard.common.distributedlock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hango.cloud.dashboard.common.constant.LockConstant;
import org.hango.cloud.dashboard.common.exception.DistributedLockTimeOutException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class MethodReentrantLockAspect {
    private final static Logger log = LoggerFactory.getLogger(MethodReentrantLockAspect.class);

    private final static ObjectMapper jsonMapper = new ObjectMapper();

    @Value("${redisKeyPrefix:apigw-portal}")
    private String redisKeyPrefix;

    @Value("${enableRedisLock:true}")
    private boolean enableRedisLock;

    // 等待redis锁超时时间（单位s）
    @Value("${tryLockTimeout:10}")
    private long tryLockTimeout;

    // 锁占用时间
    @Value("${leaseLockTimeout:10}")
    private long leaseLockTime;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(methodReentrantLock)")
    public Object around(ProceedingJoinPoint pjp, MethodReentrantLock methodReentrantLock) throws Throwable {

        if (!enableRedisLock) {
            return pjp.proceed();
        }

        String key = getKey(pjp, methodReentrantLock);
        RLock lock = null;

        try {
            lock = redissonClient.getLock(key);
        } catch (Exception e) {
            log.warn("{} Get Redis lock failed, error message: {}", LockConstant.REDIS_LOCK_LOG_PRE, e.getMessage());
        }

        // 获取锁异常直接执行方法（可能是redis或redisson本身的异常，不能阻塞方法）
        if (lock == null) {
            return pjp.proceed();
        }

        // lock
        boolean isLocked = false;
        try {
            if (lock.tryLock(tryLockTimeout, leaseLockTime, TimeUnit.SECONDS)) {
                isLocked = true;
            } else {
                // 获取锁超时抛出异常不再执行此次提交
                throw new DistributedLockTimeOutException("try lock time out.");
            }
        } catch (Exception e) {
            log.warn("{} Redis lock error, error message: {}", LockConstant.REDIS_LOCK_LOG_PRE, e.getMessage());
        }

        // execute and unlock
        try {
            return pjp.proceed();
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }

    private String getValue(Object o) {
        if (o == null) {
            return "null";
        }
        String json = null;

        // 根据流类型填字符串信息
        if (o instanceof ServletRequest) {
            json = "ServletRequest";
        } else if (o instanceof ServletResponse) {
            json = "ServletResponse";
        } else if (o instanceof InputStream) {
            json = "InputStream";
        } else if (o instanceof OutputStream) {
            json = "OutputStream";
        } else if (o instanceof MultipartFile) {
            MultipartFile file = (MultipartFile) o;
            json = "MultipartFile:" + file.getOriginalFilename();
        }
        if (json != null) {
            return json;
        }
        // 普通对象转JSON
        try {
            json = jsonMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("{} Object transform to String JSON fail, err msg: {}", LockConstant.REDIS_LOCK_LOG_PRE, e.getMessage());
        }
        if (json == null) {
            json = "{}";
        }
        return json;
    }

    private String getKey(ProceedingJoinPoint pjp, MethodReentrantLock methodReentrantLock) {
        String clazzName = pjp.getTarget().getClass().getName();
        String methodName = pjp.getSignature().getName();
        Object[] params = pjp.getArgs();

        // 生成函数作用域注解的redis key (Type.METHOD)
        StringBuilder keyBuilder = new StringBuilder(redisKeyPrefix)
                .append(":lock:method:")
                .append(clazzName)
                .append("#")
                .append(methodName);

        // 生成接口和参数两者一起作为唯一判断条件的redis key (Type.PARAM)
        generateParamKey(methodReentrantLock, params, keyBuilder);

        return keyBuilder.toString();
    }

    private void generateParamKey(MethodReentrantLock methodReentrantLock, Object[] params, StringBuilder keyBuilder) {
        int[] indexes = methodReentrantLock.value();

        if (methodReentrantLock.type() == MethodReentrantLock.Type.PARAM) {
            keyBuilder.append("(");
            if (ArrayUtils.isNotEmpty(params)) {
                if (ArrayUtils.isEmpty(indexes)) {
                    for (Object param : params) {
                        keyBuilder.append(getValue(param)).append(",");
                    }
                    keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                } else {
                    int count = 0;
                    for (int index : indexes) {
                        if (index < 0 || index >= params.length) {
                            continue;
                        }
                        keyBuilder.append(getValue(params[index])).append(",");
                        count++;
                    }
                    if (count > 0) {
                        keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                    }
                }
            }
            keyBuilder.append(")");
        }
    }
}

