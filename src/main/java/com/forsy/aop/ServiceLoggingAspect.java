package com.forsy.aop;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class ServiceLoggingAspect {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Defines a pointcut that matches all public methods within the
   * service layer (any class inside {@code com.forsy.service} and its sub-packages).
   */
  @Pointcut("execution(public * com.forsy.service..*(..))")
  public void serviceLayerPointcut() {
    // Pointcut signature
  }

  /**
   * Intercepts method execution to log entry, exit, arguments, return values,
   * and total execution time.
   *
   * <p>Logs at the DEBUG level. If DEBUG is not enabled, the method proceeds
   * with minimal overhead.
   *
   * @param joinPoint the contextual information about the intercepted method call
   * @return the result of the intercepted method's execution
   * @throws Throwable if the intercepted method throws an exception
   */
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

  /**
   * Intercepts exceptions thrown by methods within the service layer pointcut
   * and logs them at the ERROR level before re-throwing.
   *
   * @param joinPoint the contextual information about the intercepted method call
   * @param e         the exception thrown by the intercepted method
   */
  @AfterThrowing(pointcut = "serviceLayerPointcut()", throwing = "e")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();

    log.error("Exception in {}.{}() with cause = '{}' and exception = '{}'",
        className, methodName,
        e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);
  }
}
