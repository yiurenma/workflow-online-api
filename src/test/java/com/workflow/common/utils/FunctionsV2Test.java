package com.workflow.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

class FunctionsV2Test {
    @Mock
    Logger log;
    @InjectMocks
    FunctionsV2 functionsV2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTimeZoneConvert() {
        Object result = functionsV2.timeZoneConvert("1722258058000", "Asia/Kuala_Lumpur", "yyyy/MM/dd");
        Assertions.assertEquals("2024/07/29", result);
    }

    @Test
    void testTimeZoneConvert2() {
        Object result = functionsV2.timeZoneConvert("2024-07-29T14:15:09+00:00", "yyyy-MM-dd'T'HH:mm:ssXXX", "Asia/Kuala_Lumpur", "yyyy-MM-dd'T'HH:mm:ssXXX");
        Assertions.assertEquals("2024-07-29T22:15:09+08:00", result);
    }

    @Test
    void testSubString() {
        Object result = functionsV2.subString("originString", "1", "2");
        Assertions.assertEquals("r", result);
    }

    @Test
    void testGetValueOfJsonKey() throws JsonProcessingException {
        String jsonData = """
                {
                                "MAMC___": {
                                    "action": "",
                                    "actioned": "Authorised",
                                    "beanAction": "been authorised",
                                    "byPayee": "",
                                    "deeplink": "mobilex://mandate/shortview?consentId=<<<$.messageInformation.Document.MndtAccptncRpt.UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.MndtId>>>",
                                    "emailDetail": "<<<$.messageInformation.enrichInformation.payeeName>>> can now initiate payments for this agreement.",
                                    "emailSubject": "",
                                    "furtherDetail": "They can now initiate payments for this agreement.",
                                    "reason": "",
                                    "userCase": "PostActionNotificationNonCritical"
                                }}""";
        Object result = functionsV2.getValueOfJsonKey(jsonData, "MAMC___", "");
        Assertions.assertEquals(new ObjectMapper().readTree("""
                {
                                    "action": "",
                                    "actioned": "Authorised",
                                    "beanAction": "been authorised",
                                    "byPayee": "",
                                    "deeplink": "mobilex://mandate/shortview?consentId=<<<$.messageInformation.Document.MndtAccptncRpt.UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.MndtId>>>",
                                    "emailDetail": "<<<$.messageInformation.enrichInformation.payeeName>>> can now initiate payments for this agreement.",
                                    "emailSubject": "",
                                    "furtherDetail": "They can now initiate payments for this agreement.",
                                    "reason": "",
                                    "userCase": "PostActionNotificationNonCritical"
                                }"""), new ObjectMapper().readTree(result.toString()));
    }

    @Test
    void testMaskString() {
        Object result = functionsV2.maskString("originString", "\\S(?=.*\\S(?:\\s*\\S){3}\\s*$)", "*");
        Assertions.assertEquals("********ring", result);
    }

    @Test
    void testFormatNumber() {
        Object result = functionsV2.formatNumber("1000.00", "GBP");
        Object result1 = functionsV2.formatNumber("10009900978.00787987", "JPY");
        Object result2 = functionsV2.formatNumber("1000.98778", "BHD");
        Object result4 = functionsV2.formatNumber("1000.90000", "BHD");
        Object result3 = functionsV2.formatNumber("0", "CLF");
        Assertions.assertEquals("1,000.00", result);
        Assertions.assertEquals("10,009,900,978", result1);
        Assertions.assertEquals("1,000.988", result2);
        Assertions.assertEquals("1,000.900", result4);
        Assertions.assertEquals("0.0000", result3);
    }

    @ParameterizedTest
    @CsvSource({
            "1 payment, 1 payment",
            "3 payment, 3 payments",
            "5 fly, 5 flies",
            "9 branch, 9 branches"
    })
    void testPluralize(String phrase, String pluralPhrase) {
        Assertions.assertEquals(functionsV2.pluralize(phrase), pluralPhrase);
    }

    @ParameterizedTest
    @CsvSource({
            "ad-hoc, Ad-hoc",
            "3 payments daily, 3 payments daily",
            "every 4 hours per day, Every 4 hours per day",
            "quarterly starting in January, Quarterly starting in January"
    })
    void testCapitalize(String sentence, String capitalizedSentence) {
        Assertions.assertEquals(functionsV2.capitalize(sentence), capitalizedSentence);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme