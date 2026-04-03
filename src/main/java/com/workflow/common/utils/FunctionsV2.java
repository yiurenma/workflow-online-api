package com.workflow.common.utils;


import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Currency;
import java.util.Date;

@Slf4j
public class FunctionsV2 {
    /**
     * convert timestamp to formatted date time
     * **/
    public Object timeZoneConvert(String timestamp, String expectedTimeZone, String expectedDateTimeFormat){
        return
                new Date(Long.parseLong(timestamp))
                        .toInstant()
                        .atZone(ZoneId.of(expectedTimeZone))
                        .format(DateTimeFormatter.ofPattern(expectedDateTimeFormat));
    }

    /**
     * convert date time with zone to formatted date time
     * **/
    public Object timeZoneConvert(String inputDateTimeWithTimeZone, String inputDateTimeFormat, String expectedTimeZone, String expectedDateTimeFormat){
        return
                ZonedDateTime
                        .parse(inputDateTimeWithTimeZone, DateTimeFormatter.ofPattern(inputDateTimeFormat))
                        .withZoneSameInstant(ZoneId.of(expectedTimeZone))
                        .format(DateTimeFormatter.ofPattern(expectedDateTimeFormat));
    }

    /**
     * get sub string from string
     * **/
    public Object subString(String originString, String beginIndex, String endIndex){
        return originString.substring(Integer.parseInt(beginIndex), Integer.parseInt(endIndex));
    }

    /**
     * get json value from special key
     * **/
    public Object getValueOfJsonKey(String jsonData, String key, String defaultValue){
        JSONObject data = JSONObject.parseObject(jsonData);
        Object returnObject = data.get(key);
        if (ObjectUtils.isEmpty(returnObject)) return defaultValue;
        return returnObject;
    }

    /**
     * mask string
     * eg: \\S(?=.*\\S(?:\\s*\\S){3}\\s*$) mask non-space chars except last 4 non-space chars
     * **/
    public Object maskString(String originString, String maskPattern, String maskChar) {
        return originString.replaceAll(maskPattern, maskChar);
    }

    /**
     * format number
     * eg: 10000.00 then it is 10,000.00; 10000000.909090 then it is 10,000,000.90
     * **/
    public Object formatNumber(String originNumber, String currencyCode) {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
        if (StringUtils.isNotEmpty(currencyCode)){
            nf.setMaximumFractionDigits(Currency.getInstance(currencyCode).getDefaultFractionDigits());
            nf.setMinimumFractionDigits(Currency.getInstance(currencyCode).getDefaultFractionDigits());
        }
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
        return nf.format(new BigDecimal(originNumber));
    }

    /**
     * pluralize noun according to the amount
     * input format: "${amount} ${noun}"
     * e.g. 3 payment → 3 payments
     * **/
    public Object pluralize(String str) {
        String[] parts = str.split(" ");
        if (parts.length != 2) {
            log.warn("input string must be in the format '${amount} ${noun}'");
            return str;
        }

        int amount;
        try {
            amount = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            log.warn("input string's ${amount} must be a number");
            return str;
        }

        String noun = parts[1];
        if (amount > 1) {
            noun = Language.toPlural(noun);
        }

        return amount + " " + noun;
    }

    /**
     * capitalize the first char of a string
     * **/
    public Object capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        char capitalizedFirstChar = Character.toUpperCase(str.charAt(0));
        return capitalizedFirstChar + str.substring(1);
    }

    /**
     * Encode a string into a base64 string
     */
    public Object base64EncodeToString(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}
