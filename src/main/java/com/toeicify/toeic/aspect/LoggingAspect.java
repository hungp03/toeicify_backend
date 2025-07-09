package com.toeicify.toeic.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by hungpham on 7/7/2025
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.toeicify.toeic.service..*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        logBefore(joinPoint, "Service");
    }

    @AfterReturning(value = "execution(* com.toeicify.toeic.service..*(..))")
    public void logAfterReturningService(JoinPoint joinPoint) {
        logAfterReturning(joinPoint,"Service");
    }

    @AfterThrowing(value = "execution(* com.toeicify.toeic.exception..*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String clientIp = getClientIp();
        String method = joinPoint.getSignature().toShortString();

        String logMessage = String.format("[ERROR] Method: %s | IP: %s | Exception: %s", method, clientIp, ex.getMessage());

        log.error(logMessage);
        saveLogToFile(logMessage);
    }

    @Around("execution(* com.toeicify.toeic.controller..*(..))")
    public Object logExecutionTimeController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "Controller");
    }

    @Around("execution(* com.toeicify.toeic.service..*(..))")
    public Object logExecutionTimeService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "Service");
    }


    private void logBefore(JoinPoint joinPoint, String layer) {
        String clientIp = getClientIp();
        log.info("[{}] BEFORE: {} | IP: {}", layer, joinPoint.getSignature().toShortString(), clientIp);
    }

    private void logAfterReturning(JoinPoint joinPoint, String layer) {
        log.info("[{}] AFTER RETURNING: {}", layer, joinPoint.getSignature().toShortString());
    }

    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        Instant start = Instant.now();
        Object result = joinPoint.proceed();
        Instant end = Instant.now();

        Duration duration = Duration.between(start, end);
        log.info("[{}] EXECUTED: {} | Time: {} ms", layer, joinPoint.getSignature().toShortString(), duration.toMillis());

        return result;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }

    private void saveLogToFile(String message) {
        try (FileWriter writer = new FileWriter("logs/app.log", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            log.error("Could not write log to file", e);
        }
    }
}
