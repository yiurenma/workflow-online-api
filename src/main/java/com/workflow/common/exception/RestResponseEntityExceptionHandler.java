package com.workflow.common.exception;

import com.workflow.common.utils.HTTPConstant;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, ServletRequestBindingException.class, HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<GeneralError> handleBadRequestException(final Exception exception, final WebRequest request) {
        return toResponseEntity(exception, request, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST_INVALID_PARAM);
    }

    @ExceptionHandler({RestClientResponseException.class, Exception.class})
    public ResponseEntity<GeneralError> handleErrorResponseException(final Exception exception, final WebRequest request) {
        return unknownException(exception, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_SERVER_ERROR_CODE);
    }

    @ExceptionHandler({AuthorisationErrorException.class})
    public ResponseEntity<GeneralError> handleAuthorisationResponseException(final AuthorisationErrorException e, final WebRequest request) {
        if (e.getErrorInfos() == null) {
            return toResponseEntity(e, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_SERVER_ERROR_CODE);
        } else {
            GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                    request.getHeader(HTTPConstant.SESSION_CORRELATION));

            generalError.setErrorInfo(e.getErrorInfos());
            return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.FORBIDDEN);
        }
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<GeneralError> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception, final WebRequest request) {
        BindingResult result = exception.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        List<ErrorInfo> errList = new ArrayList<ErrorInfo>();
        for (int i=0; i<fieldErrors.size(); i++){
            ErrorInfo errInfo = new ErrorInfo();
            errInfo.setCode(ErrorCode.BAD_REQUEST_INVALID_PARAM);
            errInfo.setDetail(new Detail(fieldErrors.get(i).getDefaultMessage()));
            errList.add(errInfo);
        }

        GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                request.getHeader(HTTPConstant.SESSION_CORRELATION));
        generalError.setErrorInfo(errList);
        return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<GeneralError> badRequest(final Exception exception, final WebRequest request) {
        List<FieldError> fieldErrorList =  ((BindException) exception).getFieldErrors();
        List<ErrorInfo> errList = new ArrayList<ErrorInfo>();
        for (FieldError fieldError : fieldErrorList) {
            ErrorInfo errInfo = new ErrorInfo();
            errInfo.setCode(ErrorCode.BAD_REQUEST_INVALID_PARAM);
            errInfo.setDetail(new Detail(fieldError.getField() + " " + fieldError.getDefaultMessage() + ";"));
            errList.add(errInfo);
        }
        GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                request.getHeader(HTTPConstant.SESSION_CORRELATION));
        generalError.setErrorInfo(errList);
        return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<GeneralError> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception, final WebRequest request) {
        List<ErrorInfo> errList = new ArrayList<ErrorInfo>();
        ErrorInfo errInfo = new ErrorInfo();
        errInfo.setCode(ErrorCode.BAD_REQUEST_INVALID_PARAM);
        errInfo.setDetail(new Detail(exception.getParameter().getParameterName() + " should be type of " + exception.getParameter().getParameter().getParameterizedType()));
        errList.add(errInfo);

        GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                request.getHeader(HTTPConstant.SESSION_CORRELATION));
        generalError.setErrorInfo(errList);
        return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BaseErrorException.class})
    public ResponseEntity<GeneralError> handleBaseErrorException(final BaseErrorException e, final WebRequest request) {
        if (e.getErrorInfos() == null) {
            return toResponseEntity(e, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_SERVER_ERROR_CODE);
        } else {
            GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                    request.getHeader(HTTPConstant.SESSION_CORRELATION));

            generalError.setErrorInfo(e.getErrorInfos());

            //check if error is external internal error .
            for (ErrorInfo errorInfo: e.getErrorInfos()){
                if (ErrorCode.EXTERNAL_SERVER_ERROR_CODE.equals(errorInfo.getCode())){
                    return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            return new ResponseEntity<>(generalError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<GeneralError> unknownException(final Exception exception, final WebRequest request,
                                                          HttpStatus httpStatus, String errorCode) {
        List<ErrorInfo> errorInfos = new ArrayList<>();
        ErrorInfo errorInfo = new ErrorInfo(errorCode, new Detail(exception.getMessage()));
        errorInfos.add(errorInfo);
        GeneralError generalError = new GeneralError(request.getHeader(HTTPConstant.REQUEST_CORRELATION),
                request.getHeader(HTTPConstant.SESSION_CORRELATION));
        generalError.setErrorInfo(errorInfos);
        log.error("Throw Error in {}: {}", exception.getClass().getName(), generalError, exception);
        return new ResponseEntity<>(generalError, new HttpHeaders(), httpStatus);
    }

    private ResponseEntity<GeneralError> toResponseEntity(final Exception exception, final WebRequest request,
        HttpStatus httpStatus, String errorCode) {
        return unknownException(exception, request, httpStatus, errorCode);
    }

}
