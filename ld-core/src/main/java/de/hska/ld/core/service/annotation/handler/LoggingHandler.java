package de.hska.ld.core.service.annotation.handler;

import de.hska.ld.core.service.annotation.Logging;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingHandler {

    private ThreadLocal<Object[]> threadLocal = new ThreadLocal<>();

    @Before("execution(* *.*(..)) && @annotation(logging)")
    public void before(JoinPoint joinPoint, Logging logging) {
        threadLocal.set(joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* *.*(..)) && @annotation(logging)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Logging logging, Object result) {
        // TODO
        threadLocal.remove();
    }
}
