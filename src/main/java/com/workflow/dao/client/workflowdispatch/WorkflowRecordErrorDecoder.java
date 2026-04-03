package com.workflow.dao.client.workflowdispatch;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.workflow.common.exception.BaseErrorException;
import com.workflow.common.exception.Detail;
import com.workflow.common.exception.ErrorInfo;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WorkflowRecordErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String body;
        try {
            body = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return e;
        }
        try {
            JSONObject responseJsonObject = JSON.parseObject(body);

            List<ErrorInfo> errList = new ArrayList<>();

            JSONArray errorInfoJsonArray = responseJsonObject.getJSONArray("errorInfo");
            for (int i = 0; i < errorInfoJsonArray.size(); i++) {
                ErrorInfo errInfo = new ErrorInfo();
                errInfo.setCode(errorInfoJsonArray.getJSONObject(i).getString("code"));
                errInfo.setDetail(new Detail(errorInfoJsonArray.getJSONObject(i).getJSONObject("detail").getString("cause")));
                errList.add(errInfo);
            }
            return BaseErrorException.withErrorList(errList);
        } catch (Exception e) {
            String errorMsg =
                    "Can not handle workflow record API response: " + response.status() + " " + response.reason() + " " + response.request().url() + " "
                            + body;
            return BaseErrorException.withErrorCodeAndErrorDetails(String.valueOf(response.status()), errorMsg);
        }
    }
}
