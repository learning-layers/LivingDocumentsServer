package de.hska.ld.content.persistence.repository.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RepositoryAspectJoinPoint {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Before("execution(public * org.springframework.data.repository.CrudRepository+.*(..))")
    public void before(JoinPoint joinPoint) {
        System.out.println("Arguments: " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "execution(public * org.springframework.data.repository.CrudRepository+.*(..))",
            returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Returned: " + result);
    }
}
