package com.bird.sso.web.ex;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 16:18
 */
@ControllerAdvice
@Slf4j
public class DefaultSSOHandler {


    @ExceptionHandler(SSOException.class)
    @ResponseBody
    public RES handleSSOException(SSOException e) {
        log.error(e.getMessage(), e);
        return RES.of(e.getCode(),
                e.getMsg());
    }


    /**
     * 处理所有自定义异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RES handleException(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {
            return RES.of(HttpStatus.BAD_REQUEST.value(),
                    e.getMessage());
        }
        log.error(e.getMessage(), e);
        return RES.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "网络异常，请稍后重试~");
    }


    /**
     * 处理网络拥塞异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public RES<String> handleException(BlockException e) {
        log.error(e.getMessage(), e);
        return RES.of(HttpStatus.TOO_MANY_REQUESTS.value()
                , "认证中心发生网络阻塞，请稍后重试！");
    }
}

