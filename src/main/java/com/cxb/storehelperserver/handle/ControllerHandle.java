package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.controller.request.IValid;
import com.cxb.storehelperserver.service.SessionService;
import com.cxb.storehelperserver.util.RestResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * desc: web 请求处理，session校验
 * auth: cxb
 * date: 2022/11/30
 */
@Slf4j
@Aspect
@Component
public class ControllerHandle {
    @Resource
    private SessionService sessionService;

    @Pointcut("execution (* com.cxb.storehelperserver.controller.*.*(..))")
    public void logger() {
    }

    /**
     * desc: 记录传入参数
     */
    @Before(value = "logger()")
    public void beforeAdvice(JoinPoint joinPoint) {
        // 获取请求地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取参数信息
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object[] obj = joinPoint.getArgs();
            log.info("--->" + request.getRequestURI() + ":" + objectMapper.writeValueAsString(obj[0]));
        } catch (JsonProcessingException e) {
            log.warn("--->" + request.getRequestURI() + ":{json error}");
        }
    }

    /**
     * desc: 记录返回值
     */
    @AfterReturning(value = "logger()", returning = "result")
    public void afterAdvice(JoinPoint joinPoint, Object result) {
        // 获取请求地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取返回信息
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object[] obj = joinPoint.getArgs();
            log.info("<---" + request.getRequestURI() + ":" + objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            log.warn("<---" + request.getRequestURI() + ":{json error}");
        }
    }

    /**
     * desc: session 校验
     */
    @Around(value = "logger()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        Object[] obj = joinPoint.getArgs();

        // 会话白名单页面
        boolean check = false;
        if (url.equals("/api/account/login") || url.equals("/api/account/register")) {
            check = true;
        } else {
            // 验证会话
            String key = request.getHeader("token");
            int uid = sessionService.check(key);
            IValid req = (IValid) obj[0];
            if (uid == req.getId()) {
                check = true;
            }
        }

        // 验证成功继续执行，验证失败则直接返回
        if (check) {
            return joinPoint.proceed();
        } else {
            // 获取参数信息
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                log.info("--->" + url + ":" + objectMapper.writeValueAsString(obj[0]));
            } catch (JsonProcessingException e) {
                log.warn("--->" + url + ":{json error}");
                return RestResult.fail(-2, "传入参数异常");
            }

            // 直接返回
            log.info("<---" + url + ":{\"code\":-1,\"msg\":\"账号登陆信息已过期\",\"data\":{}}");
            return RestResult.fail(-3, "账号登陆信息已过期，请重新登陆");
        }
    }
}
