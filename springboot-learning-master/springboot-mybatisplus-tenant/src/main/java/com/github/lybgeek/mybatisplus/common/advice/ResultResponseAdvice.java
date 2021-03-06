package com.github.lybgeek.mybatisplus.common.advice;

import com.alibaba.druid.sql.parser.ParserException;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;

import com.github.lybgeek.mybatisplus.common.exception.BusinessException;
import com.github.lybgeek.mybatisplus.common.model.Result;
import com.github.lybgeek.mybatisplus.common.model.ValidMsg;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice(basePackages = "com.github.lybgeek")
@Slf4j
public class ResultResponseAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (Objects.isNull(o)) {
            return Result.success();
        }

        if (o instanceof Result) {
            return o;
        }

        return Result.success(o);
    }



    // ???????????????
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        String msg = e.getMessage();
        if(Strings.isNullOrEmpty(msg)){
            msg = "???????????????";
        }
        log.error("???????????????", e);
        return Result.error(msg, Result.CODE_FAIL);
    }

    /**
     * ???????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ParserException.class, SQLException.class, MybatisPlusException.class,
            MyBatisSystemException.class, org.apache.ibatis.exceptions.PersistenceException.class,
            BadSqlGrammarException.class
    })
    public Result dbException(Exception e) {

        String msg = "??????????????????" + e.getMessage();
        log.error(msg, e);
        return Result.error(msg, Result.CODE_FAIL);
    }

    /**
     * ??????????????????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleException(DuplicateKeyException e)
    {
        log.error("??????????????????????????????", e);
        return Result.error("??????????????????????????????", Result.CODE_FAIL);
    }

    /**
     *??????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleException(ConstraintViolationException e)
    {
        log.error("??????????????????", e);
        return Result.error("??????????????????", Result.CODE_FAIL);
    }

    /**
     * ???????????????????????????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleException(MethodArgumentNotValidException e)
    {
        BindingResult result = e.getBindingResult();
        return getValidateExceptionAjaxResult(e, result);
    }

    /**
     * ???????????????????????????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(BindException.class)
    public Result handleException(BindException e)
    {
        BindingResult result = e.getBindingResult();
        return getValidateExceptionAjaxResult(e, result);
    }



    /**
     * ??????????????????
     * @param e
     * @param result
     * @return
     */
    private Result getValidateExceptionAjaxResult(Exception e, BindingResult result) {
        final List<FieldError> fieldErrors = result.getFieldErrors();
        List<ValidMsg> errorList = new ArrayList<>();
        for (FieldError error : fieldErrors) {
            errorList.add(new ValidMsg(null, error.getField(), error.getDefaultMessage()));
        }
        log.error("???????????????????????????????????????", e);
        return Result.error("???????????????????????????????????????", Result.CODE_FAIL,null,errorList);
    }

    /**
     *????????????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleException(MethodArgumentTypeMismatchException e)
    {
        List<ValidMsg> errorList = new ArrayList<>();
        errorList.add(new ValidMsg("", e.getName(), e.getMessage()));
        log.error("????????????????????????", e);
        return Result.error("????????????????????????", Result.CODE_FAIL,null,errorList);
    }
    /**
     *??????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleException(MissingServletRequestParameterException e)
    {
        List<ValidMsg> errorList = new ArrayList<>();
        errorList.add(new ValidMsg("", e.getParameterName(), e.getMessage()));
        log.error("??????????????????", e);
        return Result.error("????????????????????????", Result.CODE_FAIL,null,errorList);
    }

    /**
     * ?????????????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleException(HttpMessageNotReadableException e)
    {
        log.error("?????????????????????", e);
        return Result.error("?????????????????????", Result.CODE_FAIL);
    }
    /**
     * ????????????
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(BusinessException.class)
    public Result handleException(BusinessException e)
    {
        log.info("???????????????"+e.getMessage(), e);
        return Result.error(e.getMessage(), e.getErrorCode());
    }
}