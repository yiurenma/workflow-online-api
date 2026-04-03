package com.workflow.common.utils;

public class Language {

    private Language(){}
    public static String toPlural(String noun) {
        if (noun.endsWith("y") && !isVowel(noun.charAt(noun.length() - 2))) {
            return noun.substring(0, noun.length() - 1) + "ies";
        } else if (noun.endsWith("s")
                || noun.endsWith("x")
                || noun.endsWith("z")
                || noun.endsWith("ch")
                || noun.endsWith("sh")
        ) {
            return noun + "es";
        } else {
            return noun + "s";
        }
    }

    private static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }
}
