package com.example.surveyanalyze.redis.lock;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public @interface DistributedLock {
    String value() default "";
}
