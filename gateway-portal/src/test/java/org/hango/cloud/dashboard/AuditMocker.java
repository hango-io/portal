package org.hango.cloud.dashboard;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.aop.AuditAdvice;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Configuration
public class AuditMocker {
    @Bean
    public AuditAdvice auditAdvice() {
        AuditAdvice auditAdvice = Mockito.mock(AuditAdvice.class);
        when(auditAdvice.doAround(any(ProceedingJoinPoint.class), any(Audit.class))).thenReturn(null);
        return auditAdvice;
    }
}
