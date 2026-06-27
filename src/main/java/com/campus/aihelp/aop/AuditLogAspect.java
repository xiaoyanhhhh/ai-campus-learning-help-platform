package com.campus.aihelp.aop;

import com.campus.aihelp.domain.OperationLog;
import com.campus.aihelp.mapper.LogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {
    private final LogMapper logMapper;

    public AuditLogAspect(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            status = "FAILED";
            throw ex;
        } finally {
            OperationLog log = new OperationLog();
            log.setOperation(auditLog.value());
            log.setUsername(username());
            log.setIp(ip());
            log.setMethod(pjp.getSignature().toShortString());
            log.setElapsedMs(System.currentTimeMillis() - start);
            log.setStatus(status);
            logMapper.insertOperation(log);
        }
    }

    private String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : auth.getName();
    }

    private String ip() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return "";
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
