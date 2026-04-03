package com.workflow.common.utils;

import com.workflow.common.configuration.JacksonConfiguration;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SuppressWarnings("java:S1118")
public class Tools {

    public static String replaceVariables(String content, Object object, String variableBeginChars, String variableEndChars){
        if (StringUtils.isNotEmpty(content)){
            Pattern p = Pattern.compile(variableBeginChars + "([^\"]*?)" + variableEndChars);
            Matcher m = p.matcher(content);
            while (m.find()){
                String variable = m.group().replace(variableBeginChars, "").replace(variableEndChars, "");
                log.debug("find variable : {}", variable);

                String objectString = null;
                try {
                    objectString = new JacksonConfiguration().objectMapper().writeValueAsString(object);
                    String variableValue = "";
                    Object returnObject = JsonPath.parse(objectString).read(variable);
                    //jayway jsonpath default will return LinkedHashMap hence needs to make it as JSON String
                    if (returnObject.getClass().equals(LinkedHashMap.class)){
                        variableValue = JsonPath
                                .using(Configuration.builder().jsonProvider(new GsonJsonProvider()).build())
                                .parse(objectString)
                                .read(variable)
                                .toString();
                    } else {
                        variableValue = returnObject.toString();
                    }
                    content = content.replace(variableBeginChars + variable + variableEndChars, variableValue);
                } catch (Exception e) {
                    log.warn("Exception happens when parse the content : {}", e.getMessage());
                    log.info("Because of above exception, make the variable {} as blank", variable);
                    content = content.replace(variableBeginChars + variable + variableEndChars, "");
                }

            }
        }
        return content;
    }

    // Legacy numeric account masking (template helpers)
    public static String formatAccountNumber(String accountNumber, String accountType, Boolean masking) {
        if (StringUtils.isNotEmpty(accountNumber)){
            if (accountType.equals(AppConstant.LEGACY_NUMERIC_ACCOUNT_TYPE)) {
                return formatLegacyNumericAccountType(accountNumber, masking);
            }
            return formatOtherAccountType(accountNumber, masking);
        }
        return accountNumber;
    }

    private static String formatOtherAccountType(String accountNumber, boolean masking) {
        if (accountNumber.length() < 6) {
            if (masking) accountNumber = StringUtils.substring(accountNumber, 0, accountNumber.length() - 1) + "X";
        } else {
            if (masking) accountNumber = StringUtils.substring(accountNumber, 0, accountNumber.length() - 6)
                    + "XXX"
                    + StringUtils.substring(accountNumber, accountNumber.length() - 3, accountNumber.length());
        }
        return accountNumber;
    }

    /**
     *
     * Length = 12
     * 123-456XXX-XXX
     *
     * Length = 10
     * 123-4-5XXX90
     *
     * **/
    private static String formatLegacyNumericAccountType(String accountNumber, boolean masking) {
        //remove non-numeric
        accountNumber = accountNumber.replaceAll("\\D", "");
        switch (accountNumber.length()){
            case 10:
                if (masking) accountNumber = StringUtils.substring(accountNumber,0, 4) + "XXX" + StringUtils.substring(accountNumber,7, 10);
                accountNumber =
                        StringUtils.substring(accountNumber,0, 3) + "-"
                                + StringUtils.substring(accountNumber,3, 4) + "-"
                                + StringUtils.substring(accountNumber,4, 10);
                return accountNumber;
            case 12:
                if (masking) accountNumber = StringUtils.substring(accountNumber,0, 6) + "XXX" + StringUtils.substring(accountNumber,9, 12);
                accountNumber =
                        StringUtils.substring(accountNumber,0, 3) + "-"
                                + StringUtils.substring(accountNumber,3, 9) + "-"
                                + StringUtils.substring(accountNumber,9, 12);
                return accountNumber;
            default:
                return formatOtherAccountType(accountNumber, masking);
        }
    }

    public static String formatName(String name){
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
        return maskName.toString().trim();
    }

    private static void dealWithMaskNameWhenSIsNotEmpty(StringBuilder maskName, int z, String s) {
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
