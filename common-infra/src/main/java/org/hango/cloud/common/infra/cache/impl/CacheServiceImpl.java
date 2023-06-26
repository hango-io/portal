package org.hango.cloud.common.infra.cache.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.hango.cloud.common.infra.base.mapper.CacheInfoMapper;
import org.hango.cloud.common.infra.cache.ICacheService;
import org.hango.cloud.common.infra.cache.meta.CacheInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/1/5
 */

@Slf4j
@Service
public class CacheServiceImpl extends ServiceImpl<CacheInfoMapper, CacheInfo> implements ICacheService {

    private final Lock lock = new ReentrantLock();

    // 默认缓存时间 10分钟
    Long defaultExpireTime = 10 * 60L;

    @Override
    public String getValue(String key) {
        if (!StringUtils.hasText(key)) {
            return Strings.EMPTY;
        }
        LambdaQueryWrapper<CacheInfo> query = Wrappers.lambdaQuery();
        query.gt(CacheInfo::getExpireTime, LocalDateTime.now());
        query.eq(CacheInfo::getCacheKey, key);
        CacheInfo cacheInfo = getOne(query);
        return cacheInfo == null ? Strings.EMPTY : cacheInfo.getCacheValue();
    }

    @Override
    public void setValue(String key, String value, Long expire) {
        CacheInfo cacheInfo = new CacheInfo();
        cacheInfo.setCacheKey(key);
        cacheInfo.setCacheValue(value);
        //默认失效时间
        if (expire == null || expire <= 0) {
            expire = defaultExpireTime;
        }
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expire);
        long timestamp = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        cacheInfo.setExpireTime(timestamp);
        //创建或更新缓存
        LambdaQueryWrapper<CacheInfo> updateCondition = Wrappers.lambdaQuery();
        updateCondition.eq(CacheInfo::getCacheKey, key);
        saveOrUpdate(cacheInfo, updateCondition);
        //删除过期缓存
        removeExpireCache();

    }

    @Override
    public boolean hasKey(String key) {
        return StringUtils.hasText(getValue(key));
    }

    /**
     * 删除过期缓存
     */
    private void removeExpireCache() {
        LambdaQueryWrapper<CacheInfo> query = Wrappers.lambdaQuery();
        query.lt(CacheInfo::getExpireTime, LocalDateTime.now());
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                remove(query);
            }
        } catch (Exception e) {
            log.error("removeExpireCache error: {}", e.getMessage(), e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }

    }
}
