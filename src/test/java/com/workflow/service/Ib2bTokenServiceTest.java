package com.workflow.service;


import com.workflow.common.configuration.DspConfig;
import com.workflow.common.utils.AppConstant;
import com.workflow.dao.client.ib2b.Ib2bTokenClient;
import com.workflow.dao.client.ib2b.TokenResponseBody;
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

class Ib2bTokenServiceTest {
    @Mock
    Ib2bTokenClient ib2bTokenClient;
    @Mock
    DspConfig dspConfig;
    @Spy
    @InjectMocks
    Ib2bTokenService ib2bTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetIb2bToken() {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put(AppConstant.DEFAULT, "https://example.test/dsp/token");
        when(dspConfig.getUriMap()).thenReturn(stringStringHashMap);
        when(ib2bTokenClient.getIb2bToken(any(),any())).thenReturn(new TokenResponseBody("issuedToken"));

        String result = ib2bTokenService.getIb2bToken("Basic dXNlcm5hbWU6cGFzc3dvcmQ=",null);
        Assertions.assertEquals("issuedToken", result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme