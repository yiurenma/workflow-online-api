package com.workflow.common.exception;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuthorisationErrorException extends RuntimeException {

    private List<ErrorInfo> errorInfos;

    public AuthorisationErrorException(){
    }

    public AuthorisationErrorException(String message){
        super(message);
    }

    public AuthorisationErrorException(List<ErrorInfo> errorInfos) {
        this.errorInfos = errorInfos;
    }

    public static AuthorisationErrorException withErrorCodeAndErrorDetails(String errorCode, String errorDetails) {
        List<ErrorInfo> errorInfos = new ArrayList<>();
        ErrorInfo errorInfo = new ErrorInfo(errorCode, new Detail(errorDetails));
        errorInfos.add(errorInfo);
        return new AuthorisationErrorException(errorInfos);
    }

    public static AuthorisationErrorException withCodeListAndMessageList(List<String> codes, List<String> messages) {
        List<ErrorInfo> errorInfos = new ArrayList<>();
        for (int i=0; i<codes.size(); i++){
            errorInfos.add(new ErrorInfo(codes.get(i), new Detail(messages.get(i))));
        }
        return new AuthorisationErrorException(errorInfos);
    }

    public static AuthorisationErrorException withErrorList(List<ErrorInfo> errorInfos) {
        return new AuthorisationErrorException(errorInfos);
    }
}
