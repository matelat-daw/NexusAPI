package com.futureprograms.NexusAPI.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.futureprograms.NexusAPI.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        logger.debug("Executing {}.{} with args: {}", className, methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Successfully executed {}.{} in {} ms", className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing {}.{} after {} ms: {}", className, methodName, executionTime, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* com.futureprograms.NexusAPI.interfaces..*(..))")
    public Object logRepositoryExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        long startTime = System.currentTimeMillis();
        logger.trace("Database query: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > 1000) {
                logger.warn("Slow database query detected: {}.{} took {} ms", className, methodName, executionTime);
            }
            return result;
        } catch (Exception e) {
            logger.error("Database error in {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}

