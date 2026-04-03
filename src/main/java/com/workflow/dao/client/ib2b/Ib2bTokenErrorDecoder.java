package com.workflow.dao.client.ib2b;

import com.alibaba.fastjson2.JSON;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;

import java.nio.charset.StandardCharsets;

public class Ib2bTokenErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        try {
            String body = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
            Ib2bTokenExceptionResponse ib2bTokenExceptionResponse = JSON.parseObject(body, Ib2bTokenExceptionResponse.class);
            return new Ib2bTokenException("get ib2b token exception",String.valueOf(response.status()), ib2bTokenExceptionResponse.getMessage());
        } catch (Exception e) {
            return new Ib2bTokenException("get ib2b token decode exception",String.valueOf(response.status()), "Unknown IB2B Error ");
        }
    }
}
