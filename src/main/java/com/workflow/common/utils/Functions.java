package com.workflow.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.workflow.common.object.WorkflowRuntimePayload;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Functions {
    public WorkflowRuntimePayload timeZoneConvert(String inputDateTimeWithTimeZone, String inputDateTimeFormat, String expectedTimeZone, String expectedDateTimeFormat){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(inputDateTimeFormat);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(inputDateTimeWithTimeZone, formatter);

        JSONObject returnJson = new JSONObject();
        returnJson.put("timeZoneConvert", zonedDateTime.withZoneSameInstant(ZoneId.of(expectedTimeZone)).format(DateTimeFormatter.ofPattern(expectedDateTimeFormat)));
        WorkflowRuntimePayload runtimePayload = WorkflowRuntimePayload.builder().build();
        runtimePayload.setReference(returnJson);
        return runtimePayload;
    }

    public WorkflowRuntimePayload subString(String originString, String beginIndex, String endIndex){
        JSONObject returnJson = new JSONObject();
        returnJson.put("subString", originString.substring(Integer.parseInt(beginIndex), Integer.parseInt(endIndex)));
        WorkflowRuntimePayload runtimePayload = WorkflowRuntimePayload.builder().build();
        runtimePayload.setReference(returnJson);
        return runtimePayload;
    }

    public WorkflowRuntimePayload formatAccountNumber(String entityCode, String accountNumber, String accountType, String masking) {
        String formattedAccountNumber = accountNumber;

        if (AppConstant.WORKFLOW_ACCOUNT_FORMAT_ENTITY_CODE.equals(entityCode)) {
            if (StringUtils.isNotEmpty(accountNumber)){
                if (accountType.equals(AppConstant.LEGACY_NUMERIC_ACCOUNT_TYPE)) {
                    formattedAccountNumber = formatLegacyNumericAccountType(accountNumber, Boolean.parseBoolean(masking));
                } else {
                    formattedAccountNumber =  formatOtherAccountType(accountNumber, Boolean.parseBoolean(masking));
                }
            }
        }

        JSONObject returnJson = new JSONObject();
        returnJson.put("formattedAccountNumber", formattedAccountNumber);
        WorkflowRuntimePayload runtimePayload = WorkflowRuntimePayload.builder().build();
        runtimePayload.setReference(returnJson);
        return runtimePayload;
    }

    private String formatOtherAccountType(String accountNumber, boolean masking) {
        if (accountNumber.length() < 6) {
            if (masking) accountNumber = StringUtils.substring(accountNumber, 0, accountNumber.length() - 1) + "X";
        } else {
            if (masking) accountNumber = StringUtils.substring(accountNumber, 0, accountNumber.length() - 6)
                    + "XXX"
                    + StringUtils.substring(accountNumber, accountNumber.length() - 3, accountNumber.length());
        }
        return accountNumber;
    }

    private String formatLegacyNumericAccountType(String accountNumber, boolean masking) {
        //remove non-numeric
        accountNumber = accountNumber.replaceAll("\\D", "");
        switch (accountNumber.length()){
            case 10:
                if (masking) accountNumber = StringUtils.substring(accountNumber,0, 5) + "XXX" + StringUtils.substring(accountNumber,8, 9);
                accountNumber =
                        StringUtils.substring(accountNumber,0, 3) + "-"
                                + StringUtils.substring(accountNumber,3, 4) + "-"
                                + StringUtils.substring(accountNumber,4, 10);
                return accountNumber;
            case 12:
                if (masking) accountNumber = StringUtils.substring(accountNumber,0, 6) + "XXXXXX";
                accountNumber =
                        StringUtils.substring(accountNumber,0, 3) + "-"
                                + StringUtils.substring(accountNumber,3, 9) + "-"
                                + StringUtils.substring(accountNumber,9, 12);
                return accountNumber;
            case 16:
                if (masking) accountNumber = "XXXXXXXXXXXX" + StringUtils.substring(accountNumber,12, 16) ;
                accountNumber =
                        StringUtils.substring(accountNumber,0, 4) + "-"
                                + StringUtils.substring(accountNumber,4, 11) + "-"
                                + StringUtils.substring(accountNumber,11, 16);
                return accountNumber;
            default:
                return formatOtherAccountType(accountNumber, masking);
        }
    }

    public WorkflowRuntimePayload formatName(String name){
        StringBuilder maskName = new StringBuilder();
        if(StringUtils.isNotEmpty(name)){
            String[] nameList = name.split(StringUtils.SPACE);
            int z = 0;
            for (String s : nameList) {
                if (StringUtils.isNotEmpty(s)) {
                    dealWithMaskNameWhenSIsNotEmpty(maskName, z, s);
                    z++;
                } else {
                    maskName.append(StringUtils.SPACE);
                }
            }
        }

        JSONObject returnJson = new JSONObject();
        returnJson.put("formattedName", maskName.toString().trim());
        WorkflowRuntimePayload runtimePayload = WorkflowRuntimePayload.builder().build();
        runtimePayload.setReference(returnJson);
        return runtimePayload;
    }

    private void dealWithMaskNameWhenSIsNotEmpty(StringBuilder maskName, int z, String s) {
        boolean firstNotEmptyString = z == 0;
        StringBuilder maskString = new StringBuilder();
        for (int j = 0; j < s.length() && !firstNotEmptyString; j++) {
            if (j == 0) {
                maskString.append(s.charAt(0));
            } else {
                maskString.append("*");
            }
        }
        maskName.append(StringUtils.SPACE);
        if (firstNotEmptyString){
            maskName.append(s);
        } else {
            maskName.append(maskString);
        }
    }
}
