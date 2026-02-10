package com.danil.appliances.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class LoggingServices {

    @Around("execution(* com.danil.appliances.service..*(..))")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        String where = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        Loggable ann = method.getAnnotation(Loggable.class);
        if (ann == null) {
            ann = pjp.getTarget().getClass().getAnnotation(Loggable.class);
        }

        boolean logArgs = ann != null && ann.args();
        boolean logResult = ann != null && ann.result();

        long start = System.nanoTime();

        try {
            if (logArgs) {
                log.info("→ {} args={}", where, mask(pjp.getArgs()));
            }

            Object result = pjp.proceed();

            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            if (logResult) {
                log.info("← {} {}ms result={}", where, ms, shortVal(result));
            } else {
                log.debug("← {} {}ms", where, ms);
            }

            return result;

        } catch (Throwable ex) {
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            log.warn("✖ {} {}ms {}: {}",
                    where,
                    ms,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }

    private String mask(Object[] args) {
        String s = Arrays.toString(args);

        return s
                .replaceAll("(?i)password[^,\\]]*", "password=***")
                .replaceAll("(?i)card[^,\\]]*", "card=***")
                .replaceAll("(?i)token[^,\\]]*", "token=***")
                .replaceAll("(?i)secret[^,\\]]*", "secret=***");
    }

    private String shortVal(Object v) {
        if (v == null) return "null";
        String s = String.valueOf(v);
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
