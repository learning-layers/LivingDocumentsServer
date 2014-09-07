package de.hska.ld.core.service.annotation.handler;

import de.hska.ld.core.persistence.domain.LogEntry;
import de.hska.ld.core.service.annotation.Logging;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class LoggingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingHandler.class);

    private ThreadLocal<Args> threadLocal = new ThreadLocal<>();

    @Before("execution(* *.*(..)) && @annotation(logging)")
    public void before(JoinPoint joinPoint, Logging logging) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        threadLocal.set(new Args(joinPoint.getArgs(), parameterTypes));
    }

    @AfterReturning(pointcut = "execution(* *.*(..)) && @annotation(logging)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Logging logging, Object result) {

        String action;
        String methodName = joinPoint.getSignature().getName();
        Args args = null;

        if (logging.value()[0].isEmpty()) {
            action = methodName;
        } else {
            action = logging.value()[0];
        }

        if ("save".equals(methodName)) {
            args = new Args(new Object[]{result}, new Class[]{result.getClass()});
        } else if (threadLocal.get() != null) {
            args = threadLocal.get();
        }

        LogEntry logEntry = extractArgs(args, logging);
        logEntry.setAction(action);

        System.out.println(logEntry.toString());

        threadLocal.remove();
    }

    private LogEntry extractArgs(Args args, Logging logging) {
        List<Long> idList = new ArrayList<>();
        List<Class> referencesList = new ArrayList<>();
        int referencesCounter = 0;

        for (int i = 0; i < args.parameterValues.length; i++) {
            Object arg = args.parameterValues[i];
            if (arg != null && arg.getClass().isAnnotationPresent(Entity.class)) {
                try {
                    Field field = arg.getClass().getSuperclass().getDeclaredField("id");
                    field.setAccessible(true);
                    Long id = (Long) field.get(arg);
                    idList.add(id);
                    referencesList.add(arg.getClass());
                } catch (ReflectiveOperationException e) {
                    LOGGER.error(e.getMessage());
                }
            } else if (arg instanceof Long) {
                if (referencesCounter >= logging.references().length) {
                    LOGGER.error("The given ID has no annotated referenced class parameter");
                }
                Class reference = logging.references()[referencesCounter];
                if (reference != null) {
                    idList.add((Long) arg);
                    referencesList.add(reference);
                }
                referencesCounter++;
            } else if (arg == null && Long.class.equals(args.parameterTypes[i])) {
                referencesCounter++;
            }
        }

        if (idList.size() > 0 && referencesList.size() == idList.size()) {
            LogEntry logEntry = new LogEntry();
            logEntry.setIds(idList.toArray(new Long[idList.size()]));
            logEntry.setReferences(referencesList.toArray(new Class[referencesList.size()]));
            return logEntry;
        } else {
            LOGGER.error("");
            return null;
        }
    }

    static class Args {

        Args(Object[] parameterValues, Class<?>[] parameterTypes) {
            this.parameterValues = parameterValues;
            this.parameterTypes = parameterTypes;
        }

        Object[] parameterValues;
        Class<?>[] parameterTypes;
    }
}
