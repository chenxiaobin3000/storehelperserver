package com.cxb.storehelperserver.handle;

import com.cxb.storehelperserver.util.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * desc: web 异常统一捕捉处理类
 * auth: cxb
 * date: 2022/12/1
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

    /**
     * desc: 拦截所有请求异常，validate校验
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    public RestResult handleParameterVerificationException(Exception e) {
        String msg = null;
        if (e instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
            // getFieldError获取的是第一个不合法的参数(P.S.如果有多个参数不合法的话)
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                msg = fieldError.getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            // getFieldError获取的是第一个不合法的参数(P.S.如果有多个参数不合法的话)
            FieldError fieldError = ((BindException) e).getFieldError();
            if (fieldError != null) {
                msg = fieldError.getDefaultMessage();
            }
        } else if (e instanceof ConstraintViolationException) {
            // ConstraintViolationException 的 getMessage() 返回: {方法名}.{参数名}:{message}，这里只需要取后面的 message
            msg = e.getMessage();
            if (msg != null) {
                int lastIndex = msg.lastIndexOf(':');
                if (lastIndex >= 0) {
                    msg = msg.substring(lastIndex + 1).trim();
                }
            }
            // ValidationException 的其它子类异常
        } else {
            msg = "参数异常";
        }
        return RestResult.fail(msg);
    }

    /**
     * desc: 拦截 404 异常
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public RestResult exceptionHandler404(NoHandlerFoundException e) {
        return exceptionHandler(e, "无效地址");
    }

    /**
     * desc: 拦截 500 异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestResult exceptionHandler500(Exception e) {
        return exceptionHandler(e, "服务器异常");
    }

    /**
     * desc: 统一异常处理
     */
    private RestResult exceptionHandler(Exception e, String msg) {
        StringBuilder errMsg = new StringBuilder().append(e);
        for (StackTraceElement traceElement : e.getStackTrace()) {
            if (!traceElement.getClassName().startsWith("com.cxb.storehelperserver")) {
                break;
            }
            errMsg.append("\n").append(traceElement);
        }
        log.error(errMsg.toString());
        return RestResult.fail(msg);
    }
}
