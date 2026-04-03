package com.workflow.dao.client.ib2b;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name = "ib2b-token-api",
        configuration = {Ib2bTokenErrorDecoder.class})
public interface Ib2bTokenClient {

    @PostMapping
    TokenResponseBody getIb2bToken(
            URI uri,
            @RequestBody TokenRequestBody tokenRequestBody
    );
}
