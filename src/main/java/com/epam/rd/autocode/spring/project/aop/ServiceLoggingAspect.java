package com.epam.rd.autocode.spring.project.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceLoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(public * com.epam.rd.autocode.spring.project.service..*(..))")
    public void serviceLayerPointcut() {
    }

    @Around("serviceLayerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}",
                    className, methodName, Arrays.toString(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        if (log.isDebugEnabled()) {
            log.debug("Exit: {}.{}() with result = {}. Execution time = {}ms",
                    className, methodName, result, executionTime);
        }

        return result;
    }

    @AfterThrowing(pointcut = "serviceLayerPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("Exception in {}.{}() with cause = '{}' and exception = '{}'",
                className, methodName,
                e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);
    }
}
