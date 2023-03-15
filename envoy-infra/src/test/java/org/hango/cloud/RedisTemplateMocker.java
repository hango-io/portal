package org.hango.cloud;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Configuration
public class RedisTemplateMocker {
  @Bean(name = "redisson")
  public RedissonClient redisson() {
    RedissonClient redisson = Mockito.mock(RedissonClient.class);
    return redisson;
  }
}
