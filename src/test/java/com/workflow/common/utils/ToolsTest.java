package com.workflow.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

class ToolsTest {
    @Mock
    Logger log;
    @InjectMocks
    Tools tools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReplaceVariables() {
        JSONObject content = JSONObject.parseObject("""
                {
                    "paymentRail": "GT",
                    "debitAccount": {
                        "countryCode": "GB",
                        "groupMember": "DEMO_ORG",
                        "accountNumber": "001013770833",
                        "currencyCode": "USD",
                        "formattedAccount": "001-013770-833",
                        "name": "Demo User",
                        "productType": "SAV~~DDA~~SSV"
                    },
                    "creditPayeeId": "",
                    "creditAccount": {
                        "countryCode": "GB",
                        "groupMember": "DEMO_ORG",
                        "currencyCode": "GBP",
                        "accountNumber": "001013770833",
                        "identifierCode": "",
                        "name": "Demo User"
                    },
                    "debitAmount": 10.30,
                    "creditAmount": 8.05,
                    "rate": 0.78,
                    "confirmationNumber": "GXC00OKGB1",
                    "paymentDateTime": "2023-11-01T23:06:35+01:00",
                    "transactionProperties": {
                        "obsDevEnvironment": "O63",
                        "payeeFeeAmount": 0,
                        "payeeFeeCurrency": "GBP"
                    }
                }""");
        String result = Tools.replaceVariables("<<<$.paymentRail>>>", content, "<<<", ">>>");
        Assertions.assertEquals("GT", result);
    }

    @Test
    void testReplaceVariables1() {
        JSONObject content = JSONObject.parseObject("""
                {
                    "reference": {
                        "customerDetails": [{
                            "externalIdDetails": [{
                                "concurrencyToken": "2024-03-13-13.48.15.446634",
                                "concurrencyUserId": "      D00O1",
                                "customerNumber": "1724714767",
                                "externalCustomerId": "IB1724714767",
                                "externalSystemId": "IBK",
                                "linkCreationDate": "2024-03-13",
                                "linkStatus": "R",
                                "maintenanceCode": "R",
                                "objectKey": "IBKIB1724714767"
                            }]
                        },
                        {
                            "externalIdDetails": [{
                                "concurrencyToken": "2024-03-21-10.37.53.561200",
                                "concurrencyUserId": "      D00O1",
                                "customerNumber": "1724727656",
                                "externalCustomerId": "IB1724727656",
                                "externalSystemId": "IBK",
                                "linkCreationDate": "2024-03-21",
                                "linkStatus": "R",
                                "maintenanceCode": "R",
                                "objectKey": "IBKIB1724727656"
                            }]
                        }]
                    }
                }""");
        String result = Tools.replaceVariables("<<<$.reference.customerDetails[*].externalIdDetails[?(@.externalSystemId == 'IBK')].customerNumber>>>", content, "<<<", ">>>");
        Assertions.assertEquals("[\"1724714767\",\"1724727656\"]", result);
    }

    @Test
    void testReplaceVariables2() throws JsonProcessingException {
        JSONObject content = JSONObject.parseObject("""
                {
                    "messageId": "fab432e7-0322-465a-8b65-fda3c430d265",
                    "requestLocalhostName": "localhost",
                    "requestLocalhostPort": "8084",
                    "requestSearchKey": "GB0000000-000001",
                    "messageInformation": {
                        "event": "UKRFIMainNotification",
                        "rfiCaseNumber": "GB0000000-000001",
                        "ccy": "GBP",
                        "transAmount": "80.00",
                        "payeeName": "Luckboy",
                        "expiryDate": "2023-11-01T23:06:35+08:00",
                        "accountNumber": "40052131639927",
                        "enrichInformation": {
                            "jointCustomerNumberList": ["1724727656"],
                            "jointDigitalGuidList": ["IB1724727656"],
                            "jointDigitalGuidIdentityList": ["IBK"],
                            "jointMobileNumberList": ["447700900000"],
                            "jointEmailAddressList": ["testing@example.com"],
                            "jointCustomerNameTitleList": ["Mr"],
                            "jointCustomerLastNameList": ["TDAPUSHER"],
                            "digitalGuid": "IB1724714767",
                            "digitalGuidIdentity": "IBK",
                            "customerNumber": "1724714767",
                            "customerNameTitle": "Mr",
                            "customerLastName": "UkTestDataCreationPusher",
                            "customerIdentifer": "IBK",
                            "receivingBankDes": "with the receiving bank"
                        }
                    },
                    "messageContactInfo": {
                        "emailAddress": "testing@example.com",
                        "mobileNumber": "447700900000"
                    },
                    "messageEntitySetting": {
                        "createdDateTime": 1721772685525,
                        "lastModifiedDateTime": 1722393879566,
                        "id": 4,
                        "applicationName": "UK_DRFI",
                        "retry": false,
                        "tracking": false,
                        "eimId": null,
                        "defaultServiceAccount": "Basic R0ItVFdPLUNOUy1ERVY6MUIxMi04ZmViRkE2NzQ=",
                        "region": "EU"
                    },
                    "reference": {
                        "count": 1,
                        "success": {
                            "count": 1,
                            "items": ["11a49370-0251-4899-96e5-7251c97931ae"]
                        }
                    }
                }""");
        String result = Tools.replaceVariables("<<<$.messageInformation>>>", content, "<<<", ">>>");
        Assertions.assertEquals(new ObjectMapper().readTree("""
                    {
                        "event": "UKRFIMainNotification",
                        "rfiCaseNumber": "GB0000000-000001",
                        "ccy": "GBP",
                        "transAmount": "80.00",
                        "payeeName": "Luckboy",
                        "expiryDate": "2023-11-01T23:06:35+08:00",
                        "accountNumber": "40052131639927",
                        "enrichInformation": {
                            "jointCustomerNumberList": ["1724727656"],
                            "jointDigitalGuidList": ["IB1724727656"],
                            "jointDigitalGuidIdentityList": ["IBK"],
                            "jointMobileNumberList": ["447700900000"],
                            "jointEmailAddressList": ["testing@example.com"],
                            "jointCustomerNameTitleList": ["Mr"],
                            "jointCustomerLastNameList": ["TDAPUSHER"],
                            "digitalGuid": "IB1724714767",
                            "digitalGuidIdentity": "IBK",
                            "customerNumber": "1724714767",
                            "customerNameTitle": "Mr",
                            "customerLastName": "UkTestDataCreationPusher",
                            "customerIdentifer": "IBK",
                            "receivingBankDes": "with the receiving bank"
                        }
                    }"""), new ObjectMapper().readTree(result));
    }

    @Test
    void testFormatAccountNumber() {
        String result = Tools.formatAccountNumber("8789887790", AppConstant.LEGACY_NUMERIC_ACCOUNT_TYPE, Boolean.TRUE);
        Assertions.assertEquals("878-9-XXX790", result);
    }

    @Test
    void testFormatName() {
        String result = Tools.formatName("Peter Smith");
        Assertions.assertEquals("Peter S****", result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme