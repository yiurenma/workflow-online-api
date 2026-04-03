package com.workflow.dao.client.trusttoken;

import com.alibaba.fastjson2.JSON;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;

import java.nio.charset.StandardCharsets;

public class TrustTokenErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String body = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
            TrustTokenErrorBody errorBody = JSON.parseObject(body, TrustTokenErrorBody.class);
            return new TrustTokenClientException(
                    "Trust token exchange failed",
                    String.valueOf(response.status()),
                    errorBody != null ? errorBody.getMessage() : body);
        } catch (Exception e) {
            return new TrustTokenClientException(
                    "Trust token error response could not be decoded",
                    String.valueOf(response.status()),
                    "Unknown trust-token error");
        }
    }
}
