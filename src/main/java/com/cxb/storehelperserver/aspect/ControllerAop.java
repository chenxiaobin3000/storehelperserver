package com.cxb.storehelperserver.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * desc:
 * auth: cxb
 * date: 2022/11/30
 */
@Aspect
@Component
public class ControllerAop {
    private static final Logger logger = LogManager.getLogger(ControllerAop.class);

    @Pointcut("execution (* com.cxb.storehelperserver.controller.*.*(..))")
    public void logger() {
    }

    @Before(value = "logger()")
    public void beforeAdvice(JoinPoint joinPoint) {
        // 获取请求地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取参数信息
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object[] obj = joinPoint.getArgs();
            logger.info("--->" + request.getRequestURI() + ":" + objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            logger.warn("--->" + request.getRequestURI() + ":{json error}");
        }
    }

    @AfterReturning(value = "logger()", returning = "result")
    public void afterAdvice(JoinPoint joinPoint, Object result) {
        // 获取请求地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取返回信息
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object[] obj = joinPoint.getArgs();
            logger.info("<---" + request.getRequestURI() + ":" + objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            logger.warn("<---" + request.getRequestURI() + ":{json error}");
        }
    }
}
