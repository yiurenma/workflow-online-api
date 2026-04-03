package com.workflow.service;

import com.workflow.common.configuration.TrustSapiUriProperties;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.client.trusttoken.TokenResponseBody;
import com.workflow.dao.client.trusttoken.TrustTokenClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class TrustTokenServiceTest {

    @Mock
    TrustTokenClient trustTokenClient;
    @Mock
    TrustSapiUriProperties trustSapiUriProperties;
    @Spy
    @InjectMocks
    TrustTokenService trustTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTrustToken_returnsIssuedToken() {
        HashMap<String, String> uris = new HashMap<>();
        uris.put(AppConstant.DEFAULT, "https://example.test/oauth/token");
        when(trustSapiUriProperties.getUriMap()).thenReturn(uris);
        when(trustTokenClient.exchangeTrustToken(any(), any())).thenReturn(new TokenResponseBody("issuedToken"));

        String result = trustTokenService.getTrustToken("Basic dXNlcm5hbWU6cGFzc3dvcmQ=", null);
        Assertions.assertEquals("issuedToken", result);
    }

    @Test
    void getTrustToken_usesRegionalUriWhenProvided() {
        HashMap<String, String> uris = new HashMap<>();
        uris.put(AppConstant.DEFAULT, "https://example.test/default/token");
        uris.put("AP", "https://example.test/ap/token");
        when(trustSapiUriProperties.getUriMap()).thenReturn(uris);
        when(trustTokenClient.exchangeTrustToken(any(), any())).thenReturn(new TokenResponseBody("tok"));

        String result = trustTokenService.getTrustToken("Basic dXNlcm5hbWU6cGFzc3dvcmQ=", "AP");
        Assertions.assertEquals("tok", result);
    }
}
