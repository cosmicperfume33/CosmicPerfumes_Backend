package com.example.ForgeX.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Converts paise to Indian-format words, e.g. "One Thousand One Hundred And Ninety-five only". */
public final class AmountInWords {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
            "Eighteen", "Nineteen" };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

    private AmountInWords() { }

    public static String fromPaise(long paise) {
        BigDecimal rupees = BigDecimal.valueOf(paise, 2);
        long whole = rupees.setScale(0, RoundingMode.DOWN).longValue();
        int fraction = rupees.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();

        StringBuilder sb = new StringBuilder(convert(whole));
        if (fraction > 0) {
            sb.append(" and ").append(convert(fraction)).append(" Paise");
        }
        return sb.append(" only").toString();
    }

    private static String convert(long n) {
        if (n == 0) return "Zero";

        StringBuilder sb = new StringBuilder();
        sb.append(group(n / 10_000_000, "Crore"));
        n %= 10_000_000;
        sb.append(group(n / 100_000, "Lakh"));
        n %= 100_000;
        sb.append(group(n / 1_000, "Thousand"));
        n %= 1_000;
        sb.append(group(n / 100, "Hundred"));
        n %= 100;

        if (n > 0) {
            if (sb.length() > 0) sb.append("And ");
            sb.append(belowHundred((int) n)).append(" ");
        }
        return sb.toString().trim();
    }

    private static String group(long value, String label) {
        if (value == 0) return "";
        return convert(value) + " " + label + " ";
    }

    private static String belowHundred(int n) {
        if (n < 20) return ONES[n];
        String s = TENS[n / 10];
        return n % 10 == 0 ? s : s + "-" + ONES[n % 10].toLowerCase();
    }
}