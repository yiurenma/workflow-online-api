package com.workflow.common.exception;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseErrorException extends RuntimeException {

    private List<ErrorInfo> errorInfos;

    public BaseErrorException(){
    }
    public BaseErrorException(String message){
        super(message);
    }

    public BaseErrorException(List<ErrorInfo> errorInfos) {
        this.errorInfos = errorInfos;
    }

    public static BaseErrorException withErrorCodeAndErrorDetails(String errorCode, String errorDetails) {
        List<ErrorInfo> errorInfos = new ArrayList<>();
        ErrorInfo errorInfo = new ErrorInfo(errorCode, new Detail(errorDetails));
        errorInfos.add(errorInfo);
        return new BaseErrorException(errorInfos);
    }

    public static BaseErrorException withCodeListAndMessageList(List<String> codes, List<String> messages) {
        List<ErrorInfo> errorInfos = new ArrayList<>();
        for (int i=0; i<codes.size(); i++){
            errorInfos.add(new ErrorInfo(codes.get(i), new Detail(messages.get(i))));
        }
        return new BaseErrorException(errorInfos);
    }

    public static BaseErrorException withErrorList(List<ErrorInfo> errorInfos) {
        return new BaseErrorException(errorInfos);
    }
}
