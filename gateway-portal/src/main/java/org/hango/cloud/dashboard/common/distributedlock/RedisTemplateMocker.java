package org.hango.cloud.dashboard.common.distributedlock;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTemplateMocker {
    @Bean(name = "redisson")
    public RedissonClient redisson() {
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        return redisson;
    }
}
