package com.bifrost.demo.aspect;

import com.bifrost.demo.annotation.DailyLimit;
import com.bifrost.demo.dto.response.BaseResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class DailyLimitAspect {
    private final Map<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    public void refresh() {
        counterMap.clear();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleRefresh() {
        refresh();
    }

    @Around("@annotation(limitAnnotation)")
    public Object enforceDailyLimit(ProceedingJoinPoint pjp, DailyLimit limitAnnotation) throws Throwable {
        String id = limitAnnotation.id();
        int max = limitAnnotation.max();

        counterMap.putIfAbsent(id, new AtomicInteger(0));
        int current = counterMap.get(id).incrementAndGet();

        if (current > max) {
            return ResponseEntity
                    .status(HttpStatusCode.SERVICE_UNAVAILABLE)
                    .body(BaseResponse.error("Daily limit exceeded."));
        }

        return pjp.proceed();
    }
}
