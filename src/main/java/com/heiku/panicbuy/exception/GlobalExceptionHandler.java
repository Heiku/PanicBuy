package com.heiku.panicbuy.exception;


import com.heiku.panicbuy.result.CodeMsg;
import com.heiku.panicbuy.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 异常拦截器
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {


    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){

        e.printStackTrace();

        if (e instanceof  GlobalException){
            GlobalException  exception = (GlobalException) e;

            return Result.error(exception.getCodeMsg());
        }else if(e instanceof BindException){
            BindException exception = (BindException) e;

            List<ObjectError> errors = exception.getAllErrors();

            // 获取第一个error msg
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();

            return Result.error(CodeMsg.BIND_ERROR);
        }else{
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
