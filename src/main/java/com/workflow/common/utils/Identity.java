package com.workflow.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Identity {

    BUSINESS_REGISTRATION("BUSINESS_REGISTRATION","B"),
    CERTIFICATE_NUMBER("CERTIFICATE_NUMBER","C"),
    IDENTITY_CARD("IDENTITY_CARD","I"),
    PASSPORT("PASSPORT","P"),
    OTHER("OTHER","X"),
    PSEDUO("PSEDUO","Y");

    String identityDesc;
    String identityCode;
}
