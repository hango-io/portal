package org.hango.cloud.dashboard.apiserver.exception;

import com.alibaba.fastjson.JSONException;
import com.ecwid.consul.ConsulException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.RequestContextHolder;
import org.hango.cloud.dashboard.common.exception.DistributedLockTimeOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/3/26 18:52.
 */
@ControllerAdvice
public class CommonControllerAdvice extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(CommonControllerAdvice.class);

    private static Pattern missingParameterPattern = Pattern.compile("^Optional long parameter '(?<paramName>[\\w]+)' is present but cannot.*");


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public String handleInternalErrorHandler(Exception ex) {
        HttpServletRequest req = RequestContextHolder.getRequest();
        String target = req.getRequestURI();
        String action = req.getParameter(Const.ACTION);
        String version = req.getParameter(Const.VERSION);
        logger.error("G-Portal平台全局异常处理发现500异常! serviceName:{}, action:{}, version:{}, ex:", new Object[]{target, action, version, ex});
        return apiReturn(CommonErrorCode.InternalServerError);
    }

    @ResponseBody
    @ExceptionHandler(value = ConsulException.class)
    public String handleConsulExceptionHandler(ConsulException ex) {
        logger.error(ex.getMessage());
        return apiReturn(CommonErrorCode.ConsulOperationException);
    }


    /**
     * 用于解决参数类型不匹配的错误，如需要int类型却传入"name"，会返回标准的参数值非法的ErrorCode，例如serviceId="蛤蛤蛤"
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return handleError(CommonErrorCode.InvalidParameterValue(ex.getValue(), ex.getName()), ex);
    }


    /**
     * 用于解决参数类型不匹配的错误，如需要int类型却传入"name"，会返回标准的参数值非法的ErrorCode，例如serviceId="蛤蛤蛤"
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public String handleBindExceptionException(BindException ex) {
        List<ObjectError> allErrors = ex.getAllErrors();
        if (!CollectionUtils.isEmpty(allErrors)) {
            ObjectError objectError = allErrors.get(0);
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                Object rejectedValue = fieldError.getRejectedValue();
                String field = fieldError.getField();
                if (StringUtils.isBlank(String.valueOf(rejectedValue))) {
                    return apiReturn(CommonErrorCode.MissingParameter(field));
                }
                return apiReturn(CommonErrorCode.InvalidParameter(rejectedValue.toString(), field));
            }
        }
        return apiReturn(CommonErrorCode.InternalServerError);
    }


    /**
     * 用于解决参数缺失的错误，若需要传入的参数未提供，则通过该方法返回标准的参数缺失ErrorCode,例如serviceId=
     */
    @ResponseBody
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException ex) {
        Matcher matcher = missingParameterPattern.matcher(ex.getMessage());
        if (matcher.matches()) {
            String paramName = matcher.group("paramName");
            return handleError(CommonErrorCode.MissingParameter(StringUtils.capitalize(paramName)), ex);
        }
        // 如果发现不是能够识别的错误，则返回 500 。
        return handleError(CommonErrorCode.InvalidBodyFormat, ex);
    }


    /**
     * 用于解决参数缺失的错误，若需要传入的参数未提供，则通过该方法返回标准的参数缺失ErrorCode,例如serviceId 未填写
     */
    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {

        return handleError(CommonErrorCode.MissingParameter(ex.getParameterName()), ex);
    }


    /**
     * 用于处理必须传的header
     */
    @ResponseBody
    @ExceptionHandler(ServletRequestBindingException.class)
    public String handleServletRequestBindingException(ServletRequestBindingException ex) {
        return handleError(CommonErrorCode.MissingParameter(""), ex);
    }


    /**
     * 处理body不存在的异常，由于exception较泛，只能匹配message的内容判断
     */
    @ExceptionHandler
    public String handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        //需要加上对空RequestBody的处理
        if (ex.getMessage().startsWith("Required request body is missing")) {
            return handleError(CommonErrorCode.InvalidBodyFormat, ex);
        }
        if (ex.getCause() instanceof JSONException) {
            return handleError(CommonErrorCode.InvalidBodyFormat, ex);
        }
        return handleError(CommonErrorCode.InternalServerError, ex);
    }

    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    public String methodNotSupportError(Exception ex) {
        return handleError(CommonErrorCode.MethodNotAllowed, ex);
    }

    //限制上传文件的大小，但是未生效，因为超出大小抛出的异常同样为MultipartException
//    @ResponseBody
//    @ExceptionHandler(value = FileUploadBase.SizeLimitExceededException.class)
//    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
//    public String maxUploadSizeExceed(Exception ex) {
//        return handleError(CommonApiErrorCode.MaxUploadSizeExceed, ex);
//    }

    //未选择上传的文件
    @ResponseBody
    @ExceptionHandler(value = {MultipartException.class, MissingServletRequestPartException.class})
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String multipartExceptionHandle(Exception ex) {
        return handleError(CommonErrorCode.MissingUploadedFile, ex);
    }

    /**
     * 数据库字段和程序不统一
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = BadSqlGrammarException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String badSqlGrammarException(Exception ex) {

        return handleError(CommonErrorCode.InternalServerError, ex);
    }


    /**
     * 处理重复提交异常
     *
     * @param ex 异常实例
     * @return 错误信息
     */
    @ResponseBody
    @ExceptionHandler(value = DistributedLockTimeOutException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public String handleCommitLockException(Exception ex) {
        return handleError(CommonErrorCode.InvalidOperation, ex);
    }

    /**
     * 用于解决404错误
     */
    @ResponseBody
    @ExceptionHandler(value = {UnsatisfiedServletRequestParameterException.class, NoHandlerFoundException.class})
    public String noSuchApiErrorHandler(Exception ex) {
        return handleError(CommonErrorCode.NoSuchAPI, ex);
    }

    private String handleError(ErrorCode errorCode, Exception ex) {
        HttpServletRequest req = RequestContextHolder.getRequest();
        String target = req.getRequestURI();
        String action = req.getParameter(Const.ACTION);
        String version = req.getParameter(Const.VERSION);
        logger.error("G-Portal平台全局异常处理发现异常! serviceName:{}, action:{}, version:{}, e:", new Object[]{target, action, version, ex});
        return apiReturn(errorCode);
    }


    /**
     * 参数值不合要求的错误
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Object constraintViolationExceptionHandler(ConstraintViolationException ex) {
        final ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
        final String[] splits = violation.getPropertyPath().toString().split("\\.");
        return makeResponse(CommonErrorCode.InvalidParameterValue(violation.getInvalidValue(), splits[splits.length - 1], violation.getMessage()), ex, false);
    }

    /**
     * 参数解析非法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        final FieldError fieldError = ex.getBindingResult().getFieldError();
        String objectName = fieldError.getField();
        if (fieldError.getField() != null && fieldError.getField().contains(".")) {
            String[] objectNameArray = fieldError.getField().split("\\.");
            objectName = objectNameArray[objectNameArray.length - 1];
        }
//        if (StringUtils.isNotBlank(fieldError.getDefaultMessage())) {
//            return makeResponse(new ErrorCode(400, "InvalidParameterValue", fieldError.getDefaultMessage(), "the parameter " + objectName + " is invalid"), ex, false);
//        }
        return makeResponse(CommonErrorCode.InvalidParameterValue(fieldError.getRejectedValue(), StringUtils.capitalize(objectName), fieldError.getDefaultMessage()), ex, false);
    }

    /**
     * 参数解析非法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(InvalidFormatException.class)
    public Object handleInvalidFormatException(InvalidFormatException ex) {
        return makeResponse(CommonErrorCode.InvalidBodyFormat, ex, false);
    }

    /**
     * fastjson解析错误
     */
    @ExceptionHandler(value = {JSONException.class, HttpMessageConversionException.class})
    public Object jsonErrorHandler(Exception ex) {
        return makeResponse(CommonErrorCode.InvalidBodyFormat, ex, true);
    }


    private Object makeResponse(ErrorCode errorCode, Exception ex, boolean needPrint) {
        logger.info("全局异常处理捕获到了异常 ：\n " + ex + ", ");
        if (needPrint) {
            ex.printStackTrace();
        }
        return apiReturn(errorCode);
    }

}
