package com.workflow.dao.client.trusttoken;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name = "trust-token-api", configuration = {TrustTokenErrorDecoder.class})
public interface TrustTokenClient {

    @PostMapping
    TokenResponseBody exchangeTrustToken(
            URI uri,
            @RequestBody TokenRequestBody tokenRequestBody
    );
}
