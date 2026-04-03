package com.workflow.service;

import com.workflow.common.configuration.TrustSapiUriProperties;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.client.trusttoken.InputTokenState;
import com.workflow.dao.client.trusttoken.OutputTokenState;
import com.workflow.dao.client.trusttoken.TokenRequestBody;
import com.workflow.dao.client.trusttoken.TokenResponseBody;
import com.workflow.dao.client.trusttoken.TrustTokenClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Base64;

@Service
public class TrustTokenService {

    @Autowired
    TrustTokenClient trustTokenClient;
    @Autowired
    TrustSapiUriProperties trustSapiUriProperties;

    @Cacheable(key = "#basicAuthenticationHeader", value = AppConstant.trustTokenCache)
    public String getTrustToken(String basicAuthenticationHeader, String region) {
        if (StringUtils.isEmpty(region)) {
            region = AppConstant.DEFAULT;
        }
        URI uri = URI.create(trustSapiUriProperties.getUriMap().get(region));
        String serviceAccount = new String(Base64.getDecoder().decode(basicAuthenticationHeader.split(StringUtils.SPACE)[1]));
        TokenRequestBody tokenRequestBody = TokenRequestBody.builder()
                .inputTokenState(InputTokenState.builder().tokenType("CREDENTIAL")
                        .username(serviceAccount.split(":")[0])
                        .password(serviceAccount.split(":")[1])
                        .build())
                .outputTokenState(OutputTokenState.builder().tokenType("JWT").build())
                .build();
        TokenResponseBody tokenResponseBody = trustTokenClient.exchangeTrustToken(uri, tokenRequestBody);
        return tokenResponseBody.getIssuedToken();
    }
}
