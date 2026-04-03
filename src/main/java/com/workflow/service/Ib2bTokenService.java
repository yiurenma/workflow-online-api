package com.workflow.service;

import com.workflow.common.configuration.DspConfig;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.client.ib2b.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Base64;

@Service

public class Ib2bTokenService {
    @Autowired
    Ib2bTokenClient ib2bTokenClient;
    @Autowired
    DspConfig dspConfig;
    @Cacheable(key = "#basicAuthenticationHeader", value = AppConstant.ib2bTokenCache)
    public String getIb2bToken(String basicAuthenticationHeader,String region) {
        if (StringUtils.isEmpty(region)){
            region = AppConstant.DEFAULT;
        }
        URI uri = URI.create(dspConfig.getUriMap().get(region));
        String serviceAccount = new String(Base64.getDecoder().decode(basicAuthenticationHeader.split(StringUtils.SPACE)[1]));
        TokenRequestBody tokenRequestBody = TokenRequestBody.builder()
                .inputTokenState(InputTokenState.builder().tokenType("CREDENTIAL").username(serviceAccount.split(":")[0]).password(serviceAccount.split(":")[1]).build())
                .outputTokenState(OutputTokenState.builder().tokenType("JWT").build())
                .build();
        TokenResponseBody tokenResponseBody = ib2bTokenClient.getIb2bToken(uri,tokenRequestBody);
        return tokenResponseBody.getIssuedToken();
    }
}
